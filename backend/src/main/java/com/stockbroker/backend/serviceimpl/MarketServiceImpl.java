package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;
import com.stockbroker.backend.dto.StockRequest;
import com.stockbroker.backend.entity.Stock;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.StockHistoryRepository;
import com.stockbroker.backend.repository.StockRepository;
import com.stockbroker.backend.service.MarketService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketServiceImpl implements MarketService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public MarketServiceImpl(StockRepository stockRepository,
                             StockHistoryRepository stockHistoryRepository) {

        this.stockRepository = stockRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    @Override
    public StockQuoteResponse getStockQuote(String symbol) {

        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found: " + symbol));

        return mapToResponse(stock);
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
    public StockQuoteResponse createOrUpdateStock(StockRequest request) {

        Stock stock = stockRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseGet(() -> {
                    Stock created = new Stock();
                    created.setSymbol(request.getSymbol().toUpperCase());
                    created.setOpenPrice(request.getPrice());
                    created.setHighPrice(request.getPrice());
                    created.setLowPrice(request.getPrice());
                    created.setVolume(0L);
                    return created;
                });

        stock.setCompanyName(request.getCompanyName());
        stock.setCurrentPrice(request.getPrice());
        stock.setHighPrice(Math.max(stock.getHighPrice(), request.getPrice()));
        stock.setLowPrice(Math.min(stock.getLowPrice(), request.getPrice()));
        stock.setLastUpdated(LocalDateTime.now());

        return mapToResponse(stockRepository.save(stock));
    }

    @Override
    public StockQuoteResponse setCircuitHalt(String symbol, boolean halted) {

        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found: " + symbol));

        stock.setCircuitHalted(halted);

        return mapToResponse(stockRepository.save(stock));
    }

    private StockQuoteResponse mapToResponse(Stock stock) {

        StockQuoteResponse response = new StockQuoteResponse();

        response.setSymbol(stock.getSymbol());
        response.setCompanyName(stock.getCompanyName());
        response.setCurrentPrice(stock.getCurrentPrice());
        response.setOpenPrice(stock.getOpenPrice());
        response.setHighPrice(stock.getHighPrice());
        response.setLowPrice(stock.getLowPrice());
        response.setVolume(stock.getVolume());
        response.setLastUpdated(stock.getLastUpdated());
        response.setCircuitHalted(stock.isCircuitHalted());

        return response;
    }
}
