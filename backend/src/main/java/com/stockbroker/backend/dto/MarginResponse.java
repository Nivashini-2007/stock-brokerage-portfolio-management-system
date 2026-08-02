package com.stockbroker.backend.dto;

import lombok.Data;

@Data
public class MarginResponse {

    private Long clientId;

    private String clientName;

    private Double availableMargin;

    private Double usedMargin;

    private Double totalMargin;

    private Double utilizationPercentage;
}