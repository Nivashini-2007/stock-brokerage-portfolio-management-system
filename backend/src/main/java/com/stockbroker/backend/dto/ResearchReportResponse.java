package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResearchReportResponse {

    private Long id;

    private String companyName;

    private String symbol;

    private Long analystId;

    private String analystName;

    private String recommendation;

    private Double targetPrice;

    private LocalDate publishedDate;

    private String summary;

    private ReportStatus status;
}
