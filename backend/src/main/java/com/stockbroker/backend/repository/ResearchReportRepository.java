package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.ResearchReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchReportRepository
        extends JpaRepository<ResearchReport, Long> {
}