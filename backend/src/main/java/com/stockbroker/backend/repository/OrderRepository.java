package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByClientIdAndStatusOrderByPlacedAtAsc(Long clientId, OrderStatus status);

    List<Order> findByClientIdAndStockSymbolAndOrderSideAndQuantityAndPriceAndPlacedAtAfter(
            Long clientId, String stockSymbol,
            com.stockbroker.backend.enums.OrderSide orderSide,
            Integer quantity, Double price, LocalDateTime after);

    List<Order> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByParentOrderIdAndStatus(Long parentOrderId, OrderStatus status);
}