package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.ReportReviewRequest;
import com.stockbroker.backend.dto.ResearchReportRequest;
import com.stockbroker.backend.dto.ResearchReportResponse;
import com.stockbroker.backend.entity.ResearchReport;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.NotificationType;
import com.stockbroker.backend.enums.ReportStatus;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.exception.UnauthorisedAccessException;
import com.stockbroker.backend.repository.ResearchReportRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.NotificationService;
import com.stockbroker.backend.service.ResearchReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResearchReportServiceImpl implements ResearchReportService {

    private final ResearchReportRepository researchReportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ResearchReportServiceImpl(ResearchReportRepository researchReportRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService) {
        this.researchReportRepository = researchReportRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ResearchReportResponse createReport(ResearchReportRequest request) {

        User analyst = userRepository.findById(SecurityUtils.currentPrincipal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analyst not found"));

        ResearchReport report = new ResearchReport();
        report.setCompanyName(request.getCompanyName());
        report.setSymbol(request.getSymbol().toUpperCase());
        report.setAnalyst(analyst);
        report.setRecommendation(request.getRecommendation());
        report.setTargetPrice(request.getTargetPrice());
        report.setSummary(request.getSummary());
        report.setStatus(ReportStatus.PENDING_REVIEW);

        return mapToResponse(researchReportRepository.save(report));
    }

    @Override
    public ResearchReportResponse submitForReview(Long id) {

        ResearchReport report = getOwnedReport(id);
        report.setStatus(ReportStatus.PENDING_REVIEW);

        return mapToResponse(researchReportRepository.save(report));
    }

    @Override
    public ResearchReportResponse approve(Long id, ReportReviewRequest request) {

        ResearchReport report = researchReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research report not found"));

        report.setStatus(ReportStatus.PUBLISHED);
        report.setPublishedDate(LocalDate.now());

        ResearchReport saved = researchReportRepository.save(report);

        notificationService.notify(null, NotificationType.RESEARCH_PUBLISHED,
                "New research published: " + saved.getSymbol() + " - " + saved.getRecommendation());

        return mapToResponse(saved);
    }

    @Override
    public ResearchReportResponse reject(Long id, ReportReviewRequest request) {

        ResearchReport report = researchReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research report not found"));

        report.setStatus(ReportStatus.REJECTED);

        return mapToResponse(researchReportRepository.save(report));
    }

    @Override
    public List<ResearchReportResponse> getPublishedReports() {

        return researchReportRepository.findByStatus(ReportStatus.PUBLISHED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResearchReportResponse> getMyReports() {

        Long analystId = SecurityUtils.currentPrincipal().getId();

        return researchReportRepository.findByAnalystId(analystId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResearchReportResponse> getPendingReports() {

        return researchReportRepository.findByStatus(ReportStatus.PENDING_REVIEW)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ResearchReport getOwnedReport(Long id) {

        ResearchReport report = researchReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research report not found"));

        Long currentUserId = SecurityUtils.currentPrincipal().getId();

        if (!report.getAnalyst().getId().equals(currentUserId) && !SecurityUtils.isStaff()) {
            throw new UnauthorisedAccessException("You may only modify your own research reports");
        }

        return report;
    }

    private ResearchReportResponse mapToResponse(ResearchReport report) {

        ResearchReportResponse response = new ResearchReportResponse();

        response.setId(report.getId());
        response.setCompanyName(report.getCompanyName());
        response.setSymbol(report.getSymbol());
        response.setAnalystId(report.getAnalyst().getId());
        response.setAnalystName(report.getAnalyst().getFirstName() + " " + report.getAnalyst().getLastName());
        response.setRecommendation(report.getRecommendation());
        response.setTargetPrice(report.getTargetPrice());
        response.setPublishedDate(report.getPublishedDate());
        response.setSummary(report.getSummary());
        response.setStatus(report.getStatus());

        return response;
    }
}
