package com.digitalq.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatsResponse {
    private long totalWaiting;
    private long totalServedToday;
    private long activeCounters;
    private long totalCounters;
    private double averageWaitTimeMinutes;
    private List<CounterStats> counterStats;

    @Data
    @Builder
    public static class CounterStats {
        private Long counterId;
        private String counterName;
        private String serviceType;
        private boolean active;
        private boolean paused;
        private String currentToken;
        private long waitingCount;
        private long servedToday;
    }
}
