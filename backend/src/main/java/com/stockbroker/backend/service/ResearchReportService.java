package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.ReportReviewRequest;
import com.stockbroker.backend.dto.ResearchReportRequest;
import com.stockbroker.backend.dto.ResearchReportResponse;

import java.util.List;

public interface ResearchReportService {

    ResearchReportResponse createReport(ResearchReportRequest request);

    ResearchReportResponse submitForReview(Long id);

    ResearchReportResponse approve(Long id, ReportReviewRequest request);

    ResearchReportResponse reject(Long id, ReportReviewRequest request);

    List<ResearchReportResponse> getPublishedReports();

    List<ResearchReportResponse> getMyReports();

    List<ResearchReportResponse> getPendingReports();
}
