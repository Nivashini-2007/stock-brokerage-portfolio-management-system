package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.RiskAlertResponse;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.RiskAlertService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    public RiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    /**
     * Appendix A "Monitor Client Margins" / risk alerts: Risk Manager/Admin
     * see all; Dealer sees a limited view (reuses the same list here).
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('DEALER','RISK_MANAGER','ADMIN')")
    public List<RiskAlertResponse> getAlerts() {

        return riskAlertService.getAllAlerts();
    }

    @GetMapping("/alerts/{clientId}")
    public List<RiskAlertResponse> getAlertsForClient(
            @PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return riskAlertService.getAlertsForClient(clientId);
    }
}
