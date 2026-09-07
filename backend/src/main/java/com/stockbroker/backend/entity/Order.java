package com.stockbroker.backend.entity;

import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.OrderStatus;
import com.stockbroker.backend.enums.OrderType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stockSymbol;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide orderSide;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Limit price for LIMIT/BRACKET entry legs. Null for MARKET orders
     * (executed at the prevailing Stock.currentPrice).
     */
    private Double price;

    /**
     * Trigger price for STOP_LOSS / BRACKET / COVER orders.
     */
    private Double triggerPrice;

    /**
     * Target (profit-booking) price for BRACKET orders.
     */
    private Double targetPrice;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    private LocalDateTime executedAt;

    /**
     * For an auto-generated bracket/cover exit order, the entry order it
     * belongs to. Null for standalone/entry orders.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_order_id")
    private Order parentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @PrePersist
    public void prePersist() {

        this.placedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
}
