package com.digitalq.service;

import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.repository.CounterRepository;
import com.digitalq.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private CounterRepository counterRepository;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private TokenService tokenService;

    private Counter activeCounter;
    private Token waitingToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "tokenExpiryMinutes", 30);

        activeCounter = Counter.builder()
                .id(1L)
                .name("Counter 1")
                .serviceType("General")
                .active(true)
                .build();

        waitingToken = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .customerName("John Doe")
                .counterId(1L)
                .status(Token.TokenStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    void createToken_Success() {
        when(counterRepository.findById(1L)).thenReturn(Optional.of(activeCounter));
        when(tokenRepository.count()).thenReturn(0L);
        when(tokenRepository.save(any(Token.class))).thenReturn(waitingToken);

        Token result = tokenService.createToken("John Doe", 1L);

        assertNotNull(result);
        assertEquals("A-001", result.getTokenNumber());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(Token.TokenStatus.WAITING, result.getStatus());
    }

    @Test
    void createToken_CounterNotFound() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            tokenService.createToken("John", 99L);
        });
    }

    @Test
    void createToken_InactiveCounter() {
        Counter inactiveCounter = Counter.builder()
                .id(2L)
                .name("Counter 2")
                .active(false)
                .build();
        when(counterRepository.findById(2L)).thenReturn(Optional.of(inactiveCounter));

        assertThrows(IllegalArgumentException.class, () -> {
            tokenService.createToken("John", 2L);
        });
    }

    @Test
    void verifyToken_Success() {
        when(tokenRepository.findByTokenNumber("A-001")).thenReturn(Optional.of(waitingToken));

        Token result = tokenService.verifyToken("A-001");

        assertNotNull(result);
        assertEquals("A-001", result.getTokenNumber());
    }

    @Test
    void verifyToken_NotFound() {
        when(tokenRepository.findByTokenNumber("Z-999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            tokenService.verifyToken("Z-999");
        });
    }

    @Test
    void verifyToken_Expired() {
        Token expiredToken = Token.builder()
                .id(2L)
                .tokenNumber("A-002")
                .status(Token.TokenStatus.EXPIRED)
                .build();
        when(tokenRepository.findByTokenNumber("A-002")).thenReturn(Optional.of(expiredToken));

        assertThrows(IllegalStateException.class, () -> {
            tokenService.verifyToken("A-002");
        });
    }

    @Test
    void serveToken_Success() {
        Token calledToken = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .status(Token.TokenStatus.CALLED)
                .build();

        when(tokenRepository.findById(1L)).thenReturn(Optional.of(calledToken));
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token t = invocation.getArgument(0);
            t.setServedAt(LocalDateTime.now());
            return t;
        });
        when(counterRepository.findById(1L)).thenReturn(Optional.of(activeCounter));

        Token result = tokenService.serveToken(1L);

        assertEquals(Token.TokenStatus.SERVED, result.getStatus());
        assertNotNull(result.getServedAt());
    }

    @Test
    void serveToken_NotCalled() {
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(waitingToken));

        assertThrows(IllegalStateException.class, () -> {
            tokenService.serveToken(1L);
        });
    }

    @Test
    void callNextToken_Success() {
        Token token1 = Token.builder().id(1L).tokenNumber("A-001").status(Token.TokenStatus.WAITING).build();
        Token token2 = Token.builder().id(2L).tokenNumber("A-002").status(Token.TokenStatus.WAITING).build();
        List<Token> waitingTokens = Arrays.asList(token1, token2);

        when(tokenRepository.findWaitingTokensByCounter(1L)).thenReturn(waitingTokens);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(counterRepository.findById(1L)).thenReturn(Optional.of(activeCounter));

        Token result = tokenService.callNextToken(1L);

        assertEquals(Token.TokenStatus.CALLED, result.getStatus());
        assertEquals("A-001", result.getTokenNumber());
    }

    @Test
    void callNextToken_NoWaitingTokens() {
        when(tokenRepository.findWaitingTokensByCounter(1L)).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> {
            tokenService.callNextToken(1L);
        });
    }

    @Test
    void getPosition_FirstInQueue() {
        Token token1 = Token.builder().id(1L).tokenNumber("A-001").status(Token.TokenStatus.WAITING).build();
        Token token2 = Token.builder().id(2L).tokenNumber("A-002").status(Token.TokenStatus.WAITING).build();
        List<Token> waitingTokens = Arrays.asList(token1, token2);

        when(tokenRepository.findByTokenNumber("A-001")).thenReturn(Optional.of(token1));
        when(tokenRepository.findWaitingTokensByCounter(1L)).thenReturn(waitingTokens);

        Integer position = tokenService.getPosition("A-001");

        assertEquals(1, position);
    }

    @Test
    void getPosition_SecondInQueue() {
        Token token1 = Token.builder().id(1L).tokenNumber("A-001").status(Token.TokenStatus.WAITING).build();
        Token token2 = Token.builder().id(2L).tokenNumber("A-002").status(Token.TokenStatus.WAITING).build();
        List<Token> waitingTokens = Arrays.asList(token1, token2);

        when(tokenRepository.findByTokenNumber("A-002")).thenReturn(Optional.of(token2));
        when(tokenRepository.findWaitingTokensByCounter(1L)).thenReturn(waitingTokens);

        Integer position = tokenService.getPosition("A-002");

        assertEquals(2, position);
    }

    @Test
    void expireOldTokens_MarksExpired() {
        Token oldToken = Token.builder()
                .id(1L)
                .tokenNumber("A-001")
                .status(Token.TokenStatus.WAITING)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(tokenRepository.findByStatusAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(oldToken));

        tokenService.expireOldTokens();

        verify(tokenRepository, atLeastOnce()).save(any(Token.class));
    }
}
