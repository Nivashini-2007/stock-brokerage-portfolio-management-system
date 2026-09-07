package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.TaxReportResponse;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.PortfolioService;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * GET Portfolio Holdings - ownership enforced via SecurityUtils.
     */
    @GetMapping("/{clientId}")
    public List<PortfolioResponse> getPortfolio(
            @PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return portfolioService.getClientPortfolio(clientId);
    }

    /**
     * GET all client portfolios - Appendix A "View All Client Portfolios":
     * Dealer/Compliance Officer(L)/Risk Manager/Admin.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('DEALER','COMPLIANCE_OFFICER','RISK_MANAGER','ADMIN')")
    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    /**
     * GET Portfolio Performance
     */
    @GetMapping("/performance/{clientId}")
    public PortfolioPerformanceResponse getPerformance(
            @PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return portfolioService.getPortfolioPerformance(clientId);
    }

    /**
     * GET Annual Tax Report
     */
    @GetMapping("/tax/{clientId}/year/{year}")
    public TaxReportResponse getTaxReport(
            @PathVariable Long clientId,
            @PathVariable Integer year) {

        SecurityUtils.assertCanAccessClient(clientId);
        return portfolioService.getAnnualTaxReport(clientId, year);
    }

}
