package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.KycStatus;
import com.stockbroker.backend.enums.RiskProfile;
import com.stockbroker.backend.enums.TradingStatus;
import lombok.Data;

@Data
public class ClientAccountResponse {

    private Long clientId;

    private String clientName;

    private TradingStatus tradingStatus;

    private KycStatus kycStatus;

    private RiskProfile riskProfile;

    /**
     * Masked - only the last 4 characters of the PAN are shown, matching
     * standard KYC-data display conventions.
     */
    private String maskedPan;
}
