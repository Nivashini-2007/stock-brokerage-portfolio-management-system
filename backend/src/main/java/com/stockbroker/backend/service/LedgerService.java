package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.LedgerResponse;
import com.stockbroker.backend.dto.TransferRequest;
import com.stockbroker.backend.entity.Order;

import java.util.List;

public interface LedgerService {

    List<LedgerResponse> getLedger(Long clientId);

    LedgerResponse transferFunds(TransferRequest request);

    double getCurrentBalance(Long clientId);

    /**
     * Computes brokerage/GST/STT/exchange-charges/stamp-duty for an
     * executed order, books the net cash impact as one Ledger entry, and
     * persists the itemized TradeSettlement (contract note) with a T+1
     * settlement date.
     */
    void recordTradeSettlement(Order order);
}
