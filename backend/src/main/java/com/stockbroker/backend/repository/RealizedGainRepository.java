package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.RealizedGain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RealizedGainRepository extends JpaRepository<RealizedGain, Long> {

    List<RealizedGain> findByClientIdAndSellDateBetween(
            Long clientId, LocalDateTime start, LocalDateTime end);
}
