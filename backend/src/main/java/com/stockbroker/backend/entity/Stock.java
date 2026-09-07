package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private Double currentPrice;

    @Column(nullable = false)
    private Double openPrice;

    @Column(nullable = false)
    private Double highPrice;

    @Column(nullable = false)
    private Double lowPrice;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * Simplified FR4 "circuit breaker detection" - when true, new order
     * placement for this symbol is rejected. Toggled by an admin/dealer
     * endpoint rather than a real exchange feed.
     */
    @Column(nullable = false)
    private boolean circuitHalted = false;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
}