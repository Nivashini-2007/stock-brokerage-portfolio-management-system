package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;
import com.stockbroker.backend.dto.StockRequest;
import com.stockbroker.backend.service.MarketService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/market/quote/{symbol}")
    public StockQuoteResponse getStockQuote(
            @PathVariable String symbol) {

        return marketService.getStockQuote(symbol);
    }

    @GetMapping("/market/chart/{symbol}")
    public List<StockHistoryResponse> getChart(
            @PathVariable String symbol) {

        return marketService.getStockHistory(symbol);
    }

    /**
     * Admin/Dealer only - there is no reachable NSE/BSE market-data feed in
     * this environment, so prices are set manually instead.
     */
    @PostMapping("/market/stocks")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public StockQuoteResponse createOrUpdateStock(
            @Valid @RequestBody StockRequest request) {

        return marketService.createOrUpdateStock(request);
    }

    @PatchMapping("/market/quote/{symbol}/circuit-halt")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','RISK_MANAGER')")
    public StockQuoteResponse setCircuitHalt(
            @PathVariable String symbol,
            @RequestParam boolean halted) {

        return marketService.setCircuitHalt(symbol, halted);
    }
}
