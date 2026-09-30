package com.digitalq.service;

import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.repository.CounterRepository;
import com.digitalq.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final CounterRepository counterRepository;
    private final QrCodeService qrCodeService;

    @Value("${digitalq.token.expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Transactional
    public Token createToken(String customerName, Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .filter(Counter::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found or inactive: " + counterId));

        String tokenNumber = generateTokenNumber(counterId);

        LocalDateTime now = LocalDateTime.now();
        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .customerName(customerName)
                .counterId(counterId)
                .status(Token.TokenStatus.WAITING)
                .createdAt(now)
                .expiresAt(now.plus(tokenExpiryMinutes, ChronoUnit.MINUTES))
                .build();

        return tokenRepository.save(token);
    }

    private String generateTokenNumber(Long counterId) {
        String prefix = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char letter = prefix.charAt((int) (counterId % prefix.length()));

        long count = tokenRepository.count() + 1;
        return String.format("%c-%03d", letter, count);
    }

    @Transactional(readOnly = true)
    public Optional<Token> findByTokenNumber(String tokenNumber) {
        return tokenRepository.findByTokenNumber(tokenNumber);
    }

    @Transactional(readOnly = true)
    public List<Token> findWaitingTokens(Long counterId) {
        return tokenRepository.findWaitingTokensByCounter(counterId);
    }

    @Transactional(readOnly = true)
    public List<Token> findActiveTokens(Long counterId) {
        return tokenRepository.findActiveTokensByCounter(counterId);
    }

    @Transactional(readOnly = true)
    public Integer getPosition(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + tokenNumber));

        if (token.getStatus() == Token.TokenStatus.SERVED) {
            return 0;
        }
        if (token.getStatus() == Token.TokenStatus.CALLED) {
            return 1;
        }

        List<Token> waitingTokens = tokenRepository.findWaitingTokensByCounter(token.getCounterId());
        for (int i = 0; i < waitingTokens.size(); i++) {
            if (waitingTokens.get(i).getId().equals(token.getId())) {
                return i + 1;
            }
        }
        return waitingTokens.size();
    }

    @Transactional
    public Token verifyToken(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + tokenNumber));

        if (token.getStatus() == Token.TokenStatus.EXPIRED) {
            throw new IllegalStateException("Token has expired");
        }
        if (token.getStatus() == Token.TokenStatus.SERVED) {
            throw new IllegalStateException("Token has already been served");
        }
        if (token.getStatus() == Token.TokenStatus.CALLED) {
            throw new IllegalStateException("Token has already been called");
        }

        return token;
    }

    @Transactional
    public Token serveToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found: " + tokenId));

        if (token.getStatus() != Token.TokenStatus.CALLED) {
            throw new IllegalStateException("Token must be in CALLED status to be served");
        }

        token.setStatus(Token.TokenStatus.SERVED);
        token.setServedAt(LocalDateTime.now());

        Token saved = tokenRepository.save(token);

        // Clear current token on counter
        Counter counter = counterRepository.findById(token.getCounterId()).orElse(null);
        if (counter != null && token.getTokenNumber().equals(counter.getCurrentToken())) {
            counter.setCurrentToken(null);
            counterRepository.save(counter);
        }

        return saved;
    }

    @Transactional
    public Token callNextToken(Long counterId) {
        List<Token> waitingTokens = tokenRepository.findWaitingTokensByCounter(counterId);
        if (waitingTokens.isEmpty()) {
            throw new IllegalStateException("No waiting tokens for this counter");
        }

        Token nextToken = waitingTokens.get(0);
        nextToken.setStatus(Token.TokenStatus.CALLED);
        Token saved = tokenRepository.save(nextToken);

        // Update counter's current token
        Counter counter = counterRepository.findById(counterId).orElse(null);
        if (counter != null) {
            counter.setCurrentToken(saved.getTokenNumber());
            counterRepository.save(counter);
        }

        return saved;
    }

    @Transactional
    public void expireOldTokens() {
        List<Token> expiredTokens = tokenRepository.findByStatusAndExpiresAtBefore(
                Token.TokenStatus.WAITING, LocalDateTime.now());
        for (Token token : expiredTokens) {
            token.setStatus(Token.TokenStatus.EXPIRED);
            tokenRepository.save(token);
        }

        List<Token> expiredCalled = tokenRepository.findByStatusAndExpiresAtBefore(
                Token.TokenStatus.CALLED, LocalDateTime.now());
        for (Token token : expiredCalled) {
            token.setStatus(Token.TokenStatus.EXPIRED);
            tokenRepository.save(token);
        }
    }

    @Transactional(readOnly = true)
    public long countWaitingByCounter(Long counterId) {
        return tokenRepository.countByCounterIdAndStatus(counterId, Token.TokenStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public long countServedTodayByCounter(Long counterId) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return tokenRepository.countByCounterIdAndStatus(counterId, Token.TokenStatus.SERVED);
    }

    @Transactional(readOnly = true)
    public long countAllWaiting() {
        return tokenRepository.countAllWaiting();
    }

    @Transactional(readOnly = true)
    public long countServedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return tokenRepository.countServedSince(startOfDay);
    }

    @Transactional(readOnly = true)
    public double getAverageWaitTimeMinutes() {
        List<Token> servedTokens = tokenRepository.findAll().stream()
                .filter(t -> t.getStatus() == Token.TokenStatus.SERVED && t.getServedAt() != null)
                .toList();

        if (servedTokens.isEmpty()) {
            return 0.0;
        }

        double totalMinutes = servedTokens.stream()
                .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getServedAt()))
                .sum();

        return totalMinutes / servedTokens.size();
    }

    public String generateQrCode(String tokenNumber) {
        return qrCodeService.generateQrCodeBase64(tokenNumber);
    }
}
