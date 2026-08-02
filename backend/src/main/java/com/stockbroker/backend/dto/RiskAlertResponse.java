package com.stockbroker.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RiskAlertResponse {

    private Long id;

    private String severity;

    private String title;

    private String description;

    private LocalDate createdDate;
}