package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.ResearchReport;
import com.stockbroker.backend.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResearchReportRepository
        extends JpaRepository<ResearchReport, Long> {

    List<ResearchReport> findByStatus(ReportStatus status);

    List<ResearchReport> findByAnalystId(Long analystId);
}