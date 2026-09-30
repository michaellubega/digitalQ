package com.digitalq.repository;

import com.digitalq.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenNumber(String tokenNumber);
    List<Token> findByCounterIdAndStatusOrderByCreatedAtAsc(Long counterId, Token.TokenStatus status);
    List<Token> findByCounterIdAndStatusInOrderByCreatedAtAsc(Long counterId, List<Token.TokenStatus> statuses);
    List<Token> findByStatusAndExpiresAtBefore(Token.TokenStatus status, LocalDateTime now);
    long countByCounterIdAndStatus(Long counterId, Token.TokenStatus status);
    long countByStatus(Token.TokenStatus status);

    @Query("SELECT t FROM Token t WHERE t.counterId = :counterId AND t.status = 'WAITING' ORDER BY t.createdAt ASC")
    List<Token> findWaitingTokensByCounter(@Param("counterId") Long counterId);

    @Query("SELECT t FROM Token t WHERE t.counterId = :counterId AND t.status IN ('WAITING', 'CALLED') ORDER BY t.createdAt ASC")
    List<Token> findActiveTokensByCounter(@Param("counterId") Long counterId);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'WAITING'")
    long countAllWaiting();

    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'SERVED' AND t.servedAt >= :since")
    long countServedSince(@Param("since") LocalDateTime since);
}
