package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.ClientAccountResponse;
import com.stockbroker.backend.dto.KycReviewRequest;
import com.stockbroker.backend.dto.KycSubmitRequest;

public interface KycService {

    ClientAccountResponse submitKyc(KycSubmitRequest request);

    ClientAccountResponse reviewKyc(Long clientId, KycReviewRequest request);

    ClientAccountResponse getClientAccount(Long clientId);

    /**
     * Throws TradingNotAllowedException unless the client's KYC is VERIFIED
     * and their trading account is ACTIVE (SRS Appendix F).
     */
    void assertTradingAllowed(Long clientId);
}
