package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "research_reports")
@Data
public class ResearchReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String analyst;

    @Column(nullable = false)
    private String recommendation;

    @Column(nullable = false)
    private Double targetPrice;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(length = 2000)
    private String summary;
}