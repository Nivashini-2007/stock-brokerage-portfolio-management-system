package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.TaxReportResponse;
import com.stockbroker.backend.service.PortfolioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * GET Portfolio Holdings
     */
    @GetMapping("/{clientId}")
    public List<PortfolioResponse> getPortfolio(
            @PathVariable Long clientId) {

        return portfolioService.getClientPortfolio(clientId);
    }

    /**
     * GET Portfolio Performance
     */
    @GetMapping("/performance/{clientId}")
    public PortfolioPerformanceResponse getPerformance(
            @PathVariable Long clientId) {

        return portfolioService.getPortfolioPerformance(clientId);
    }

    /**
     * GET Annual Tax Report
     */
    @GetMapping("/tax/{clientId}/year/{year}")
    public TaxReportResponse getTaxReport(
            @PathVariable Long clientId,
            @PathVariable Integer year) {

        return portfolioService.getAnnualTaxReport(clientId, year);
    }

}