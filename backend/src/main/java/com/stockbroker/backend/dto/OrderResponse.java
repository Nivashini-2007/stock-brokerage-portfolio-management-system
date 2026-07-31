package com.stockbroker.backend.dto;

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

    private Integer quantity;

    private Double price;

    private Double totalAmount;

    private OrderStatus status;

    private LocalDateTime orderDate;

    private Long clientId;

    private String clientName;
}