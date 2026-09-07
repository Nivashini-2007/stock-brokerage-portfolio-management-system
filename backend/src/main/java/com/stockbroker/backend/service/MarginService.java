package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.MarginResponse;

public interface MarginService {

    MarginResponse getMargin(Long clientId);

    /**
     * Recomputes totalMargin/usedMargin/availableMargin for a client from
     * their ledger balance and currently PENDING BUY orders, and evaluates
     * the margin-call (>=80%) / auto-square-off (>=90%) thresholds.
     */
    void recalculate(Long clientId);

    /**
     * Throws InsufficientMarginException if the client cannot cover
     * orderValue given their currently available margin.
     */
    void ensureSufficientMargin(Long clientId, double orderValue);
}
