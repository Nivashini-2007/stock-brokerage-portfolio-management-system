package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.ResearchReportResponse;
import com.stockbroker.backend.dto.StockHistoryResponse;
import com.stockbroker.backend.dto.StockQuoteResponse;

import java.util.List;

public interface MarketService {

    StockQuoteResponse getStockQuote(String symbol);

    List<StockHistoryResponse> getStockHistory(String symbol);

    List<ResearchReportResponse> getResearchReports();

}