package com.stockbroker.backend.service;

import java.time.LocalDate;

public interface ComplianceService {

    /**
     * CSV export of every order executed on the given date (SRS FR8
     * "Daily Activity Report generated in exchange-prescribed format").
     * There is no real SEBI upload portal to submit to, so this returns
     * the generated file content directly.
     */
    String generateDailyActivityReportCsv(LocalDate date);

    /**
     * CSV export of active client trading codes (SRS FR8 "Unique Client
     * Code file").
     */
    String generateUccFileCsv();
}
