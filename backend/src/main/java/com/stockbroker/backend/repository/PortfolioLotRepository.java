package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.PortfolioLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioLotRepository extends JpaRepository<PortfolioLot, Long> {

    List<PortfolioLot> findByClientIdAndSymbolAndQuantityGreaterThanOrderByBuyDateAsc(
            Long clientId, String symbol, Integer quantity);

    List<PortfolioLot> findByClientIdAndSymbol(Long clientId, String symbol);
}
