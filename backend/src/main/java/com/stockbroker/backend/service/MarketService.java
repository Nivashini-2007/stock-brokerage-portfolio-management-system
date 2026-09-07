package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;
import com.stockbroker.backend.dto.StockRequest;

import java.util.List;

public interface MarketService {

    StockQuoteResponse getStockQuote(String symbol);

    List<StockHistoryResponse> getStockHistory(String symbol);

    /**
     * Admin-only: there is no reachable NSE/BSE market-data feed in this
     * environment, so prices are set/updated manually (or by a future
     * simulator) through this endpoint instead.
     */
    StockQuoteResponse createOrUpdateStock(StockRequest request);

    StockQuoteResponse setCircuitHalt(String symbol, boolean halted);
}
