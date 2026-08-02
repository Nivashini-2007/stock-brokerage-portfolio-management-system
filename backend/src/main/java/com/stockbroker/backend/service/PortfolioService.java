package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.TaxReportResponse;

import java.util.List;

public interface PortfolioService {

    List<PortfolioResponse> getClientPortfolio(Long clientId);

    PortfolioPerformanceResponse getPortfolioPerformance(Long clientId);

    TaxReportResponse getAnnualTaxReport(Long clientId, Integer year);

}