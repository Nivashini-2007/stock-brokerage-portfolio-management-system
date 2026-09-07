package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.TaxReportResponse;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.RealizedGain;
import com.stockbroker.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioService {

    List<PortfolioResponse> getClientPortfolio(Long clientId);

    List<PortfolioResponse> getAllPortfolios();

    PortfolioPerformanceResponse getPortfolioPerformance(Long clientId);

    TaxReportResponse getAnnualTaxReport(Long clientId, Integer year);

    boolean hasSufficientHoldings(Long clientId, String symbol, Integer quantity);

    /**
     * Records a new FIFO lot and updates the aggregate Portfolio row for a
     * BUY execution.
     */
    void applyBuy(User client, String symbol, String companyName,
                  Integer quantity, Double price, Order sourceOrder);

    /**
     * Consumes FIFO lots oldest-first for a SELL execution, updates the
     * aggregate Portfolio row, and returns the realized-gain slices created.
     */
    List<RealizedGain> applySell(User client, String symbol, Integer quantity,
                                  Double sellPrice, LocalDateTime sellDate);

    /**
     * Refreshes currentPrice/marketValue/profitLoss for every open holding
     * from the latest Stock price (called by PortfolioMtmScheduler).
     */
    void refreshMarkToMarket();
}
