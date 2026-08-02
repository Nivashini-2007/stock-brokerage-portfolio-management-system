package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByClientId(Long clientId);

    Optional<Portfolio> findByClientIdAndStockSymbol(Long clientId,
                                                     String stockSymbol);

}