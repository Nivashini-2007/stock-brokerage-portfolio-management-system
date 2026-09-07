package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.OrderType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Order side (BUY/SELL) is required")
    private OrderSide orderSide;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    /**
     * Limit price. Required for LIMIT/BRACKET orders, ignored for MARKET,
     * validated in OrderServiceImpl since the requirement depends on orderType.
     */
    private Double price;

    /**
     * Trigger price for STOP_LOSS / BRACKET / COVER orders.
     */
    private Double triggerPrice;

    /**
     * Target (profit-booking) price for BRACKET orders.
     */
    private Double targetPrice;
}
