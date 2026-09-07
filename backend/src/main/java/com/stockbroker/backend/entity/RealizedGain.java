package com.stockbroker.backend.entity;

import com.stockbroker.backend.enums.GainType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A closed (FIFO-matched) slice of a position, used for LTCG/STCG tax
 * reporting (SRS FR10). LTCG/STCG grandfathering (FY18 fair-market-value
 * notification) is explicitly out of scope - classification is a simple
 * holding-period (> 365 days) test.
 */
@Entity
@Table(name = "realized_gains")
@Data
public class RealizedGain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double buyPrice;

    @Column(nullable = false)
    private Double sellPrice;

    @Column(nullable = false)
    private LocalDateTime buyDate;

    @Column(nullable = false)
    private LocalDateTime sellDate;

    @Column(nullable = false)
    private Double gainAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GainType gainType;
}
