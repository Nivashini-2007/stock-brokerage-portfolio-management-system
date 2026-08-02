package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.RiskAlertResponse;

import java.util.List;

public interface RiskAlertService {

    List<RiskAlertResponse> getAllAlerts();

}