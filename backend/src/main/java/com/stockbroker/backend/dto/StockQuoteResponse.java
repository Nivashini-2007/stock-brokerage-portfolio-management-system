package com.stockbroker.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockQuoteResponse {

    private String symbol;

    private String companyName;

    private Double currentPrice;

    private Double openPrice;

    private Double highPrice;

    private Double lowPrice;

    private Long volume;

    private LocalDateTime lastUpdated;
}