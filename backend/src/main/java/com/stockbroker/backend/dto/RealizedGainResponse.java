package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.GainType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RealizedGainResponse {

    private String symbol;
    private Integer quantity;
    private Double buyPrice;
    private Double sellPrice;
    private LocalDateTime buyDate;
    private LocalDateTime sellDate;
    private Double gainAmount;
    private GainType gainType;
}
