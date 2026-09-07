package com.stockbroker.backend.scheduler;

import com.stockbroker.backend.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SRS FR5: "MTM valuation refreshes every 15 seconds during active market
 * trading hours" - trading-hours gating is not implemented (documented
 * simplification), this runs continuously.
 */
@Component
public class PortfolioMtmScheduler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioMtmScheduler.class);

    private final PortfolioService portfolioService;

    public PortfolioMtmScheduler(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Scheduled(fixedRate = 15000)
    public void run() {
        try {
            portfolioService.refreshMarkToMarket();
        } catch (Exception e) {
            log.error("Portfolio MTM scheduler failed", e);
        }
    }
}
