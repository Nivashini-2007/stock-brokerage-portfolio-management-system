package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.OrderType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class OrderRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Stock symbol is required")
    private String stockSymbol;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
}