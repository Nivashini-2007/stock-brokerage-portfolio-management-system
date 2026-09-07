package com.stockbroker.backend.controller;

import com.stockbroker.backend.service.ComplianceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/compliance")
@PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/daily-activity-report")
    public ResponseEntity<String> dailyActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String csv = complianceService.generateDailyActivityReportCsv(date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=daily-activity-report-" + date + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/ucc-file")
    public ResponseEntity<String> uccFile() {

        String csv = complianceService.generateUccFileCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ucc-file.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
