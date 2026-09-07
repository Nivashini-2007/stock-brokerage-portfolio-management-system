package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.TradeSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeSettlementRepository extends JpaRepository<TradeSettlement, Long> {

    Optional<TradeSettlement> findByOrderId(Long orderId);

    List<TradeSettlement> findBySettledFalseAndSettlementDateLessThanEqual(LocalDate date);
}
