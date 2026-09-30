package com.digitalq.service;

import com.digitalq.dto.StatsResponse;
import com.digitalq.model.Counter;
import com.digitalq.model.Token;
import com.digitalq.repository.CounterRepository;
import com.digitalq.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final TokenRepository tokenRepository;
    private final CounterRepository counterRepository;
    private final TokenService tokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60000) // Every minute
    public void expireOldTokens() {
        tokenService.expireOldTokens();
    }

    public void notifyQueueUpdate(Long counterId) {
        List<Token> activeTokens = tokenService.findActiveTokens(counterId);
        messagingTemplate.convertAndSend("/topic/queue/" + counterId, activeTokens);
    }

    public void notifyTokenCalled(String tokenNumber) {
        messagingTemplate.convertAndSend("/topic/token/" + tokenNumber, "CALLED");
    }

    public void notifyAdminStatsUpdate() {
        StatsResponse stats = getStats();
        messagingTemplate.convertAndSend("/topic/admin/stats", stats);
    }

    public StatsResponse getStats() {
        List<Counter> counters = counterRepository.findAll();
        List<StatsResponse.CounterStats> counterStats = new ArrayList<>();

        for (Counter counter : counters) {
            long waiting = tokenService.countWaitingByCounter(counter.getId());
            long servedToday = tokenService.countServedTodayByCounter(counter.getId());

            counterStats.add(StatsResponse.CounterStats.builder()
                    .counterId(counter.getId())
                    .counterName(counter.getName())
                    .serviceType(counter.getServiceType())
                    .active(counter.isActive())
                    .paused(counter.isPaused())
                    .currentToken(counter.getCurrentToken())
                    .waitingCount(waiting)
                    .servedToday(servedToday)
                    .build());
        }

        long totalWaiting = counterStats.stream().mapToLong(StatsResponse.CounterStats::getWaitingCount).sum();
        long totalServedToday = counterStats.stream().mapToLong(StatsResponse.CounterStats::getServedToday).sum();
        long activeCounters = counterStats.stream().filter(StatsResponse.CounterStats::isActive).count();

        return StatsResponse.builder()
                .totalWaiting(totalWaiting)
                .totalServedToday(totalServedToday)
                .activeCounters(activeCounters)
                .totalCounters(counters.size())
                .averageWaitTimeMinutes(tokenService.getAverageWaitTimeMinutes())
                .counterStats(counterStats)
                .build();
    }
}
