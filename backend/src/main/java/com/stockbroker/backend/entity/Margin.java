package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "margin")
@Data
public class Margin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double availableMargin;

    @Column(nullable = false)
    private Double usedMargin;

    @Column(nullable =false)
    private Double totalMargin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
}