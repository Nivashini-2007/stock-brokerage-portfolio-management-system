package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.RiskAlertResponse;
import com.stockbroker.backend.entity.RiskAlert;
import com.stockbroker.backend.repository.RiskAlertRepository;
import com.stockbroker.backend.service.RiskAlertService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskAlertServiceImpl implements RiskAlertService {

    private final RiskAlertRepository riskAlertRepository;

    public RiskAlertServiceImpl(RiskAlertRepository riskAlertRepository) {
        this.riskAlertRepository = riskAlertRepository;
    }

    @Override
    public List<RiskAlertResponse> getAllAlerts() {

        return riskAlertRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RiskAlertResponse mapToResponse(RiskAlert alert) {

        RiskAlertResponse response = new RiskAlertResponse();

        response.setId(alert.getId());
        response.setSeverity(alert.getSeverity());
        response.setTitle(alert.getTitle());
        response.setDescription(alert.getDescription());
        response.setCreatedDate(alert.getCreatedDate());

        return response;
    }
}