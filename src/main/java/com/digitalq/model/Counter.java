package com.digitalq.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "counter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "current_token", length = 20)
    private String currentToken;

    @Column(name = "paused", nullable = false)
    @Builder.Default
    private boolean paused = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
