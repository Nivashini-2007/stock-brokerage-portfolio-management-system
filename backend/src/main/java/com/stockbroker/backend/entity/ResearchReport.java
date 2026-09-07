package com.stockbroker.backend.entity;

import com.stockbroker.backend.enums.ReportStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyst_id", nullable = false)
    private User analyst;

    @Column(nullable = false)
    private String recommendation;

    @Column(nullable = false)
    private Double targetPrice;

    private LocalDate publishedDate;

    @Column(length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;
}