package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.ReportReviewRequest;
import com.stockbroker.backend.dto.ResearchReportRequest;
import com.stockbroker.backend.dto.ResearchReportResponse;
import com.stockbroker.backend.service.ResearchReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchReportService researchReportService;

    public ResearchController(ResearchReportService researchReportService) {
        this.researchReportService = researchReportService;
    }

    /**
     * Public: published research only (SRS Appendix A "View Market Data"
     * column includes Guest - permitAll wired in SecurityConfig).
     */
    @GetMapping
    public List<ResearchReportResponse> getPublished() {
        return researchReportService.getPublishedReports();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESEARCH_ANALYST')")
    public List<ResearchReportResponse> getMine() {
        return researchReportService.getMyReports();
    }

    /**
     * Compliance review queue: reports awaiting approve/reject.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public List<ResearchReportResponse> getPending() {
        return researchReportService.getPendingReports();
    }

    @PostMapping
    @PreAuthorize("hasRole('RESEARCH_ANALYST')")
    public ResponseEntity<ResearchReportResponse> create(
            @Valid @RequestBody ResearchReportRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(researchReportService.createReport(request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ResearchReportResponse approve(
            @PathVariable Long id, @RequestBody(required = false) ReportReviewRequest request) {

        return researchReportService.approve(id, request != null ? request : new ReportReviewRequest());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ResearchReportResponse reject(
            @PathVariable Long id, @RequestBody(required = false) ReportReviewRequest request) {

        return researchReportService.reject(id, request != null ? request : new ReportReviewRequest());
    }
}
