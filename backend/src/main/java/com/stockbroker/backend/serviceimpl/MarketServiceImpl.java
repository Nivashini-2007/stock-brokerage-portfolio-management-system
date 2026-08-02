package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.ResearchReportResponse;
import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;
import com.stockbroker.backend.entity.ResearchReport;
import com.stockbroker.backend.entity.Stock;
import com.stockbroker.backend.entity.StockHistory;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.ResearchReportRepository;
import com.stockbroker.backend.repository.StockHistoryRepository;
import com.stockbroker.backend.repository.StockRepository;
import com.stockbroker.backend.service.MarketService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketServiceImpl implements MarketService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final ResearchReportRepository researchReportRepository;

    public MarketServiceImpl(StockRepository stockRepository,
                             StockHistoryRepository stockHistoryRepository,
                             ResearchReportRepository researchReportRepository) {

        this.stockRepository = stockRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.researchReportRepository = researchReportRepository;
    }

    @Override
    public StockQuoteResponse getStockQuote(String symbol) {

        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found: " + symbol));

        StockQuoteResponse response = new StockQuoteResponse();

        response.setSymbol(stock.getSymbol());
        response.setCompanyName(stock.getCompanyName());
        response.setCurrentPrice(stock.getCurrentPrice());
        response.setOpenPrice(stock.getOpenPrice());
        response.setHighPrice(stock.getHighPrice());
        response.setLowPrice(stock.getLowPrice());
        response.setVolume(stock.getVolume());
        response.setLastUpdated(stock.getLastUpdated());

        return response;
    }

    @Override
    public List<StockHistoryResponse> getStockHistory(String symbol) {

        return stockHistoryRepository
                .findBySymbolOrderByTradingDateAsc(symbol.toUpperCase())
                .stream()
                .map(history -> {

                    StockHistoryResponse response = new StockHistoryResponse();

                    response.setTradingDate(history.getTradingDate());
                    response.setOpenPrice(history.getOpenPrice());
                    response.setHighPrice(history.getHighPrice());
                    response.setLowPrice(history.getLowPrice());
                    response.setClosePrice(history.getClosePrice());
                    response.setVolume(history.getVolume());

                    return response;

                }).collect(Collectors.toList());
    }

    @Override
    public List<ResearchReportResponse> getResearchReports() {

        return researchReportRepository.findAll()
                .stream()
                .map(this::mapToResearchResponse)
                .collect(Collectors.toList());
    }

    private ResearchReportResponse mapToResearchResponse(ResearchReport report) {

        ResearchReportResponse response = new ResearchReportResponse();

        response.setId(report.getId());
        response.setCompanyName(report.getCompanyName());
        response.setSymbol(report.getSymbol());
        response.setAnalyst(report.getAnalyst());
        response.setRecommendation(report.getRecommendation());
        response.setTargetPrice(report.getTargetPrice());
        response.setPublishedDate(report.getPublishedDate());
        response.setSummary(report.getSummary());

        return response;
    }
}