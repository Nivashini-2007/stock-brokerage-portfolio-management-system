package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.OrderRequest;
import com.stockbroker.backend.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByClient(Long clientId);

    List<OrderResponse> getAllOrders();

    void cancelOrder(Long id);
}