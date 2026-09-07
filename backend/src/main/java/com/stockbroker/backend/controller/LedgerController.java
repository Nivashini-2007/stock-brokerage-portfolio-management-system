package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.LedgerResponse;
import com.stockbroker.backend.dto.TransferRequest;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<LedgerResponse>> getLedger(
            @PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);

        return ResponseEntity.ok(
                ledgerService.getLedger(clientId)
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<LedgerResponse> transferFunds(
            @Valid @RequestBody TransferRequest request) {

        SecurityUtils.assertCanAccessClient(request.getClientId());

        return new ResponseEntity<>(
                ledgerService.transferFunds(request),
                HttpStatus.CREATED
        );
    }
}
