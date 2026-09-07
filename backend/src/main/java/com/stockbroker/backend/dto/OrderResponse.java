package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.OrderStatus;
import com.stockbroker.backend.enums.OrderType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponse {

    private Long id;

    private String stockSymbol;

    private String companyName;

    private OrderType orderType;

    private OrderSide orderSide;

    private Integer quantity;

    private Double price;

    private Double triggerPrice;

    private Double targetPrice;

    private Double totalAmount;

    private OrderStatus status;

    private LocalDateTime placedAt;

    private LocalDateTime executedAt;

    private Long clientId;

    private String clientName;
}
