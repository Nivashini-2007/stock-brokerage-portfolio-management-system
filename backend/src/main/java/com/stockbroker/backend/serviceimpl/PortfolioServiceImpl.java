package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.config.TradingProperties;
import com.stockbroker.backend.dto.PortfolioPerformanceResponse;
import com.stockbroker.backend.dto.PortfolioResponse;
import com.stockbroker.backend.dto.RealizedGainResponse;
import com.stockbroker.backend.dto.TaxReportResponse;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.Portfolio;
import com.stockbroker.backend.entity.PortfolioLot;
import com.stockbroker.backend.entity.RealizedGain;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.GainType;
import com.stockbroker.backend.exception.InsufficientHoldingsException;
import com.stockbroker.backend.repository.PortfolioLotRepository;
import com.stockbroker.backend.repository.PortfolioRepository;
import com.stockbroker.backend.repository.RealizedGainRepository;
import com.stockbroker.backend.repository.StockRepository;
import com.stockbroker.backend.service.PortfolioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioLotRepository portfolioLotRepository;
    private final RealizedGainRepository realizedGainRepository;
    private final StockRepository stockRepository;
    private final TradingProperties tradingProperties;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                 PortfolioLotRepository portfolioLotRepository,
                                 RealizedGainRepository realizedGainRepository,
                                 StockRepository stockRepository,
                                 TradingProperties tradingProperties) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioLotRepository = portfolioLotRepository;
        this.realizedGainRepository = realizedGainRepository;
        this.stockRepository = stockRepository;
        this.tradingProperties = tradingProperties;
    }

    @Override
    public List<PortfolioResponse> getClientPortfolio(Long clientId) {

        return portfolioRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioResponse> getAllPortfolios() {

        return portfolioRepository.findAll()
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
    public boolean hasSufficientHoldings(Long clientId, String symbol, Integer quantity) {

        return portfolioRepository.findByClientIdAndStockSymbol(clientId, symbol)
                .map(p -> p.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public void applyBuy(User client, String symbol, String companyName,
                          Integer quantity, Double price, Order sourceOrder) {

        PortfolioLot lot = new PortfolioLot();
        lot.setClient(client);
        lot.setSymbol(symbol);
        lot.setQuantity(quantity);
        lot.setBuyPrice(price);
        lot.setBuyDate(LocalDateTime.now());
        lot.setSourceOrder(sourceOrder);
        portfolioLotRepository.save(lot);

        Optional<Portfolio> optionalPortfolio =
                portfolioRepository.findByClientIdAndStockSymbol(client.getId(), symbol);

        Portfolio portfolio;

        if (optionalPortfolio.isPresent()) {

            portfolio = optionalPortfolio.get();

            int oldQuantity = portfolio.getQuantity();
            int newQuantity = oldQuantity + quantity;

            double averageBuyPrice =
                    ((portfolio.getAverageBuyPrice() * oldQuantity)
                            + (price * quantity))
                            / newQuantity;

            portfolio.setQuantity(newQuantity);
            portfolio.setAverageBuyPrice(averageBuyPrice);
            portfolio.setCurrentPrice(price);

        } else {

            portfolio = new Portfolio();
            portfolio.setClient(client);
            portfolio.setStockSymbol(symbol);
            portfolio.setCompanyName(companyName);
            portfolio.setQuantity(quantity);
            portfolio.setAverageBuyPrice(price);
            portfolio.setCurrentPrice(price);
        }

        portfolio.setMarketValue(portfolio.getQuantity() * portfolio.getCurrentPrice());
        portfolio.setProfitLoss(
                (portfolio.getCurrentPrice() - portfolio.getAverageBuyPrice())
                        * portfolio.getQuantity());

        portfolioRepository.save(portfolio);
    }

    @Override
    @Transactional
    public List<RealizedGain> applySell(User client, String symbol, Integer quantity,
                                         Double sellPrice, LocalDateTime sellDate) {

        List<PortfolioLot> lots = portfolioLotRepository
                .findByClientIdAndSymbolAndQuantityGreaterThanOrderByBuyDateAsc(
                        client.getId(), symbol, 0);

        int remaining = quantity;
        List<RealizedGain> gains = new ArrayList<>();

        for (PortfolioLot lot : lots) {

            if (remaining <= 0) {
                break;
            }

            int consumeQty = Math.min(remaining, lot.getQuantity());

            RealizedGain gain = new RealizedGain();
            gain.setClient(client);
            gain.setSymbol(symbol);
            gain.setQuantity(consumeQty);
            gain.setBuyPrice(lot.getBuyPrice());
            gain.setSellPrice(sellPrice);
            gain.setBuyDate(lot.getBuyDate());
            gain.setSellDate(sellDate);
            gain.setGainAmount((sellPrice - lot.getBuyPrice()) * consumeQty);

            long holdingDays = Duration.between(lot.getBuyDate(), sellDate).toDays();
            gain.setGainType(holdingDays > tradingProperties.getLongTermHoldingDays()
                    ? GainType.LTCG : GainType.STCG);

            gains.add(gain);

            lot.setQuantity(lot.getQuantity() - consumeQty);
            portfolioLotRepository.save(lot);

            remaining -= consumeQty;
        }

        if (remaining > 0) {
            throw new InsufficientHoldingsException(
                    "Not enough shares of " + symbol + " available to sell");
        }

        realizedGainRepository.saveAll(gains);

        Portfolio portfolio = portfolioRepository.findByClientIdAndStockSymbol(client.getId(), symbol)
                .orElseThrow(() -> new InsufficientHoldingsException(
                        "No holdings found for " + symbol));

        int newQuantity = portfolio.getQuantity() - quantity;
        portfolio.setCurrentPrice(sellPrice);

        if (newQuantity > 0) {

            List<PortfolioLot> remainingLots =
                    portfolioLotRepository.findByClientIdAndSymbol(client.getId(), symbol);

            double totalQty = remainingLots.stream().mapToInt(PortfolioLot::getQuantity).sum();
            double totalCost = remainingLots.stream()
                    .mapToDouble(l -> l.getQuantity() * l.getBuyPrice()).sum();

            portfolio.setQuantity(newQuantity);
            portfolio.setAverageBuyPrice(totalQty > 0 ? totalCost / totalQty : 0.0);
            portfolio.setMarketValue(portfolio.getQuantity() * portfolio.getCurrentPrice());
            portfolio.setProfitLoss(
                    (portfolio.getCurrentPrice() - portfolio.getAverageBuyPrice())
                            * portfolio.getQuantity());

            portfolioRepository.save(portfolio);

        } else {

            portfolioRepository.delete(portfolio);
        }

        return gains;
    }

    @Override
    @Transactional
    public void refreshMarkToMarket() {

        List<Portfolio> portfolios = portfolioRepository.findAll();

        for (Portfolio portfolio : portfolios) {

            stockRepository.findBySymbol(portfolio.getStockSymbol()).ifPresent(stock -> {

                portfolio.setCurrentPrice(stock.getCurrentPrice());
                portfolio.setMarketValue(portfolio.getQuantity() * portfolio.getCurrentPrice());
                portfolio.setProfitLoss(
                        (portfolio.getCurrentPrice() - portfolio.getAverageBuyPrice())
                                * portfolio.getQuantity());

                portfolioRepository.save(portfolio);
            });
        }
    }

    @Override
    public TaxReportResponse getAnnualTaxReport(Long clientId, Integer year) {

        // Indian financial year: 1 Apr (year) - 31 Mar (year+1)
        LocalDateTime start = LocalDateTime.of(year, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 3, 31, 23, 59, 59);

        List<RealizedGain> gains =
                realizedGainRepository.findByClientIdAndSellDateBetween(clientId, start, end);

        double shortTermGain = gains.stream()
                .filter(g -> g.getGainType() == GainType.STCG)
                .mapToDouble(RealizedGain::getGainAmount)
                .sum();

        double longTermGain = gains.stream()
                .filter(g -> g.getGainType() == GainType.LTCG)
                .mapToDouble(RealizedGain::getGainAmount)
                .sum();

        double ltcgExemptionApplied = Math.min(
                Math.max(longTermGain, 0), tradingProperties.getLtcgExemption());

        double ltcgTaxable = Math.max(longTermGain - tradingProperties.getLtcgExemption(), 0);
        double stcgTaxable = Math.max(shortTermGain, 0);

        double estimatedLtcgTax = ltcgTaxable * tradingProperties.getLtcgTaxRate();
        double estimatedStcgTax = stcgTaxable * tradingProperties.getStcgTaxRate();

        List<Portfolio> portfolios = portfolioRepository.findByClientId(clientId);

        double totalInvestment = portfolios.stream()
                .mapToDouble(p -> p.getAverageBuyPrice() * p.getQuantity()).sum();
        double totalCurrentValue = portfolios.stream()
                .mapToDouble(Portfolio::getMarketValue).sum();
        double unrealizedProfit = portfolios.stream()
                .mapToDouble(Portfolio::getProfitLoss).sum();

        TaxReportResponse response = new TaxReportResponse();

        response.setClientId(clientId);
        response.setYear(year);
        response.setTotalInvestment(totalInvestment);
        response.setTotalCurrentValue(totalCurrentValue);
        response.setUnrealizedProfit(unrealizedProfit);
        response.setShortTermGain(shortTermGain);
        response.setLongTermGain(longTermGain);
        response.setTotalRealizedProfit(shortTermGain + longTermGain);
        response.setRealizedGains(gains.stream().map(this::mapGainToResponse).collect(Collectors.toList()));
        response.setLtcgExemptionApplied(ltcgExemptionApplied);
        response.setLtcgTaxableAmount(ltcgTaxable);
        response.setStcgTaxableAmount(stcgTaxable);
        response.setEstimatedLtcgTax(estimatedLtcgTax);
        response.setEstimatedStcgTax(estimatedStcgTax);
        response.setTotalEstimatedTax(estimatedLtcgTax + estimatedStcgTax);

        return response;
    }

    private RealizedGainResponse mapGainToResponse(RealizedGain gain) {

        RealizedGainResponse response = new RealizedGainResponse();

        response.setSymbol(gain.getSymbol());
        response.setQuantity(gain.getQuantity());
        response.setBuyPrice(gain.getBuyPrice());
        response.setSellPrice(gain.getSellPrice());
        response.setBuyDate(gain.getBuyDate());
        response.setSellDate(gain.getSellDate());
        response.setGainAmount(gain.getGainAmount());
        response.setGainType(gain.getGainType());

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
                        + portfolio.getClient().getLastName());

        return response;
    }
}
