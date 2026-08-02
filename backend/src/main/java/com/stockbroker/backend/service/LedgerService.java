package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.LedgerResponse;
import com.stockbroker.backend.dto.TransferRequest;

import java.util.List;

public interface LedgerService {

    List<LedgerResponse> getLedger(Long clientId);

    LedgerResponse transferFunds(TransferRequest request);

}