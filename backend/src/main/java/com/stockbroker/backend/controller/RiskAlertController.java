package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.RiskAlertResponse;
import com.stockbroker.backend.service.RiskAlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    public RiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping("/alerts")
    public List<RiskAlertResponse> getAlerts() {

        return riskAlertService.getAllAlerts();
    }
}