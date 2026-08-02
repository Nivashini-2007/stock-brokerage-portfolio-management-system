package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.ResearchReportResponse;
import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;
import com.stockbroker.backend.service.MarketService;
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

    @GetMapping("/research")
    public List<ResearchReportResponse> getResearchReports() {

        return marketService.getResearchReports();
    }
}