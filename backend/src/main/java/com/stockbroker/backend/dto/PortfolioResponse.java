package com.stockbroker.backend.dto;

import lombok.Data;

@Data
public class PortfolioResponse {

    private Long id;

    private String stockSymbol;

    private String companyName;

    private Integer quantity;

    private Double averageBuyPrice;

    private Double currentPrice;

    private Double marketValue;

    private Double profitLoss;

    private Long clientId;

    private String clientName;

}