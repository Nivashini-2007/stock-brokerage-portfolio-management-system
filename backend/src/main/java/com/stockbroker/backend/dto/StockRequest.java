package com.stockbroker.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
}
