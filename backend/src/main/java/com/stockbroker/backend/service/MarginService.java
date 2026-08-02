package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.MarginResponse;

public interface MarginService {

    MarginResponse getMargin(Long clientId);

}