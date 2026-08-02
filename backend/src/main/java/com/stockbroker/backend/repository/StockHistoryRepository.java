package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

    List<StockHistory> findBySymbolOrderByTradingDateAsc(String symbol);

}