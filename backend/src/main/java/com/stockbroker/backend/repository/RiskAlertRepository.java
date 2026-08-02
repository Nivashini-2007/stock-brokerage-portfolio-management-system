package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

}