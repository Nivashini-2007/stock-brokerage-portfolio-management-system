package com.stockbroker.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StockHistoryResponse {

    private LocalDate tradingDate;

    private Double openPrice;

    private Double highPrice;

    private Double lowPrice;

    private Double closePrice;

    private Long volume;
}