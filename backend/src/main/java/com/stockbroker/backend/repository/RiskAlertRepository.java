package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    List<RiskAlert> findByClientIdOrderByCreatedDateDesc(Long clientId);
}