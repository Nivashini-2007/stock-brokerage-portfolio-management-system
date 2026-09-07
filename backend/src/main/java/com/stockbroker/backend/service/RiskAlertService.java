package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.RiskAlertResponse;
import com.stockbroker.backend.entity.User;

import java.util.List;

public interface RiskAlertService {

    List<RiskAlertResponse> getAllAlerts();

    List<RiskAlertResponse> getAlertsForClient(Long clientId);

    /**
     * @param client null for a system-wide alert (e.g. circuit breaker).
     */
    void createAlert(User client, String severity, String title, String description);
}
