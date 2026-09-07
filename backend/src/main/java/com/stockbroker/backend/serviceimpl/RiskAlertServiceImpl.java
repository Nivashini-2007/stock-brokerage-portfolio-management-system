package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.RiskAlertResponse;
import com.stockbroker.backend.entity.RiskAlert;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.repository.RiskAlertRepository;
import com.stockbroker.backend.service.RiskAlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Override
    public List<RiskAlertResponse> getAlertsForClient(Long clientId) {

        return riskAlertRepository.findByClientIdOrderByCreatedDateDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void createAlert(User client, String severity, String title, String description) {

        RiskAlert alert = new RiskAlert();

        alert.setClient(client);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setCreatedDate(LocalDate.now());

        riskAlertRepository.save(alert);
    }

    private RiskAlertResponse mapToResponse(RiskAlert alert) {

        RiskAlertResponse response = new RiskAlertResponse();

        response.setId(alert.getId());
        response.setSeverity(alert.getSeverity());
        response.setTitle(alert.getTitle());
        response.setDescription(alert.getDescription());
        response.setCreatedDate(alert.getCreatedDate());
        response.setClientId(alert.getClient() != null ? alert.getClient().getId() : null);

        return response;
    }
}
