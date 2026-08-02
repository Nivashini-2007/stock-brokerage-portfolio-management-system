package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.MarginResponse;
import com.stockbroker.backend.entity.Margin;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.MarginRepository;
import com.stockbroker.backend.service.MarginService;
import org.springframework.stereotype.Service;

@Service
public class MarginServiceImpl implements MarginService {

    private final MarginRepository marginRepository;

    public MarginServiceImpl(MarginRepository marginRepository) {
        this.marginRepository = marginRepository;
    }

    @Override
    public MarginResponse getMargin(Long clientId) {

        Margin margin = marginRepository.findByClientId(clientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Margin details not found for client : " + clientId));

        MarginResponse response = new MarginResponse();

        response.setClientId(margin.getClient().getId());
        response.setClientName(
                margin.getClient().getFirstName() + " " +
                margin.getClient().getLastName());

        response.setAvailableMargin(margin.getAvailableMargin());
        response.setUsedMargin(margin.getUsedMargin());
        response.setTotalMargin(margin.getTotalMargin());

        double utilization = 0;

        if (margin.getTotalMargin() != null && margin.getTotalMargin() > 0) {
            utilization =
                    (margin.getUsedMargin() / margin.getTotalMargin()) * 100;
        }

        response.setUtilizationPercentage(utilization);

        return response;
    }
}