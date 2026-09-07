package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Charge breakdown / contract-note and T+1 settlement tracking for one
 * executed order (SRS FR7). Ledger balance is updated immediately at
 * execution for usability in this demo system; `settled` is the T+1 marker
 * flipped by a daily scheduled job once settlementDate has passed.
 */
@Entity
@Table(name = "trade_settlements")
@Data
public class TradeSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Double grossAmount;

    @Column(nullable = false)
    private Double brokerage;

    @Column(nullable = false)
    private Double gst;

    @Column(nullable = false)
    private Double stt;

    @Column(nullable = false)
    private Double exchangeCharges;

    @Column(nullable = false)
    private Double stampDuty;

    @Column(nullable = false)
    private Double netAmount;

    @Column(nullable = false)
    private LocalDate tradeDate;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private boolean settled = false;
}
