package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.TaxReportResponse;
import com.stockbroker.backend.entity.Portfolio;
import com.stockbroker.backend.repository.PortfolioRepository;
import com.stockbroker.backend.service.PortfolioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    public List<PortfolioResponse> getClientPortfolio(Long clientId) {

        return portfolioRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioPerformanceResponse getPortfolioPerformance(Long clientId) {

        List<Portfolio> portfolios = portfolioRepository.findByClientId(clientId);

        PortfolioPerformanceResponse response = new PortfolioPerformanceResponse();

        response.setClientId(clientId);

        double totalInvestment = 0.0;
        double currentValue = 0.0;
        double totalProfitLoss = 0.0;

        for (Portfolio portfolio : portfolios) {

            totalInvestment +=
                    portfolio.getAverageBuyPrice() *
                    portfolio.getQuantity();

            currentValue += portfolio.getMarketValue();

            totalProfitLoss += portfolio.getProfitLoss();
        }

        response.setTotalInvestment(totalInvestment);
        response.setCurrentValue(currentValue);
        response.setTotalProfitLoss(totalProfitLoss);

        if (totalInvestment > 0) {

            response.setProfitLossPercentage(
                    (totalProfitLoss / totalInvestment) * 100
            );

        } else {

            response.setProfitLossPercentage(0.0);
        }

        response.setTotalStocks(portfolios.size());

        return response;
    }

    @Override
    public TaxReportResponse getAnnualTaxReport(Long clientId, Integer year) {

        List<Portfolio> portfolios = portfolioRepository.findByClientId(clientId);

        TaxReportResponse response = new TaxReportResponse();

        response.setClientId(clientId);
        response.setYear(year);

        double totalInvestment = 0.0;
        double totalCurrentValue = 0.0;
        double unrealizedProfit = 0.0;

        for (Portfolio portfolio : portfolios) {

            totalInvestment +=
                    portfolio.getAverageBuyPrice()
                            * portfolio.getQuantity();

            totalCurrentValue += portfolio.getMarketValue();

            unrealizedProfit += portfolio.getProfitLoss();
        }

        // SELL orders are not implemented yet
        double realizedProfit = 0.0;

        double totalProfit = realizedProfit + unrealizedProfit;

        // Only realized profit is taxable
        double taxableProfit = realizedProfit;

        // Estimated tax (15%)
        double estimatedTax = taxableProfit * 0.15;

        response.setTotalInvestment(totalInvestment);
        response.setTotalCurrentValue(totalCurrentValue);
        response.setRealizedProfit(realizedProfit);
        response.setUnrealizedProfit(unrealizedProfit);
        response.setTotalProfit(totalProfit);
        response.setTaxableProfit(taxableProfit);
        response.setEstimatedTax(estimatedTax);

        return response;
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {

        PortfolioResponse response = new PortfolioResponse();

        response.setId(portfolio.getId());
        response.setStockSymbol(portfolio.getStockSymbol());
        response.setCompanyName(portfolio.getCompanyName());
        response.setQuantity(portfolio.getQuantity());
        response.setAverageBuyPrice(portfolio.getAverageBuyPrice());
        response.setCurrentPrice(portfolio.getCurrentPrice());
        response.setMarketValue(portfolio.getMarketValue());
        response.setProfitLoss(portfolio.getProfitLoss());

        response.setClientId(portfolio.getClient().getId());

        response.setClientName(
                portfolio.getClient().getFirstName()
                        + " "
                        + portfolio.getClient().getLastName()
        );

        return response;
    }
}