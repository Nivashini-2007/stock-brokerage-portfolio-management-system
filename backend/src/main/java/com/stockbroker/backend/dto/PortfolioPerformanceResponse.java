package com.stockbroker.backend.dto;

import lombok.Data;

@Data
public class PortfolioPerformanceResponse {

    private Long clientId;

    private Double totalInvestment;

    private Double currentValue;

    private Double totalProfitLoss;

    private Double profitLossPercentage;

    private Integer totalStocks;
}