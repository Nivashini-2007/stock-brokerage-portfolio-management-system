package com.stockbroker.backend.scheduler;

import com.stockbroker.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SRS FR5 "MTM valuation refreshes every 15 seconds" - reused here as the
 * matching interval for resting LIMIT/STOP_LOSS/BRACKET/COVER orders,
 * since there is no real exchange feed pushing fills. Trading-hours
 * gating is intentionally not implemented (documented simplification) -
 * this runs continuously.
 */
@Component
public class OrderMatchingScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderMatchingScheduler.class);

    private final OrderService orderService;

    public OrderMatchingScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 15000)
    public void run() {
        try {
            orderService.tryExecutePendingOrders();
        } catch (Exception e) {
            log.error("Order matching scheduler failed", e);
        }
    }
}
