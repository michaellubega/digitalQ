package com.digitalq.service;

import com.digitalq.dto.StatsResponse;
import com.digitalq.model.Counter;
import com.digitalq.repository.CounterRepository;
import com.digitalq.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private CounterRepository counterRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private QueueService queueService;

    private Counter counter1;
    private Counter counter2;

    @BeforeEach
    void setUp() {
        counter1 = Counter.builder()
                .id(1L)
                .name("Counter 1")
                .serviceType("General")
                .active(true)
                .paused(false)
                .build();

        counter2 = Counter.builder()
                .id(2L)
                .name("Counter 2")
                .serviceType("Billing")
                .active(true)
                .paused(false)
                .build();
    }

    @Test
    void getStats_ReturnsCorrectStats() {
        List<Counter> counters = Arrays.asList(counter1, counter2);
        when(counterRepository.findAll()).thenReturn(counters);
        when(tokenService.countWaitingByCounter(1L)).thenReturn(5L);
        when(tokenService.countWaitingByCounter(2L)).thenReturn(3L);
        when(tokenService.countServedTodayByCounter(1L)).thenReturn(10L);
        when(tokenService.countServedTodayByCounter(2L)).thenReturn(7L);
        when(tokenService.getAverageWaitTimeMinutes()).thenReturn(15.5);

        StatsResponse stats = queueService.getStats();

        assertEquals(8, stats.getTotalWaiting());
        assertEquals(17, stats.getTotalServedToday());
        assertEquals(2, stats.getActiveCounters());
        assertEquals(2, stats.getTotalCounters());
        assertEquals(15.5, stats.getAverageWaitTimeMinutes());
        assertEquals(2, stats.getCounterStats().size());
    }

    @Test
    void getStats_NoCounters() {
        when(counterRepository.findAll()).thenReturn(List.of());

        StatsResponse stats = queueService.getStats();

        assertEquals(0, stats.getTotalWaiting());
        assertEquals(0, stats.getTotalServedToday());
        assertEquals(0, stats.getActiveCounters());
        assertEquals(0, stats.getTotalCounters());
    }

    @Test
    void notifyQueueUpdate_SendsMessage() {
        when(tokenService.findActiveTokens(1L)).thenReturn(List.of());

        queueService.notifyQueueUpdate(1L);

        verify(messagingTemplate).convertAndSend(eq("/topic/queue/1"), any());
    }

    @Test
    void notifyTokenCalled_SendsMessage() {
        queueService.notifyTokenCalled("A-001");

        verify(messagingTemplate).convertAndSend("/topic/token/A-001", "CALLED");
    }

    @Test
    void notifyAdminStatsUpdate_SendsStats() {
        List<Counter> counters = List.of(counter1);
        when(counterRepository.findAll()).thenReturn(counters);
        when(tokenService.countWaitingByCounter(1L)).thenReturn(0L);
        when(tokenService.countServedTodayByCounter(1L)).thenReturn(0L);
        when(tokenService.getAverageWaitTimeMinutes()).thenReturn(0.0);

        queueService.notifyAdminStatsUpdate();

        verify(messagingTemplate).convertAndSend(eq("/topic/admin/stats"), any(StatsResponse.class));
    }
}
