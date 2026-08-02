package com.stockbroker.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ResearchReportResponse {

    private Long id;

    private String companyName;

    private String symbol;

    private String analyst;

    private String recommendation;

    private Double targetPrice;

    private LocalDate publishedDate;

    private String summary;
}