package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A single FIFO purchase lot backing a client's holding in a symbol.
 * Consumed oldest-first on SELL to compute realized gains (see RealizedGain).
 */
@Entity
@Table(name = "portfolio_lots")
@Data
public class PortfolioLot {

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
    private LocalDateTime buyDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_order_id")
    private Order sourceOrder;
}
