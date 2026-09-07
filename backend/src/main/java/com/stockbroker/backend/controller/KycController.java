package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.ClientAccountResponse;
import com.stockbroker.backend.dto.KycReviewRequest;
import com.stockbroker.backend.dto.KycSubmitRequest;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.KycService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/submit")
    public ClientAccountResponse submit(@Valid @RequestBody KycSubmitRequest request) {

        SecurityUtils.assertCanAccessClient(request.getClientId());
        return kycService.submitKyc(request);
    }

    @GetMapping("/{clientId}")
    public ClientAccountResponse get(@PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return kycService.getClientAccount(clientId);
    }

    /**
     * Appendix A: KYC review/approval is a compliance function.
     */
    @PostMapping("/{clientId}/review")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ClientAccountResponse review(
            @PathVariable Long clientId,
            @Valid @RequestBody KycReviewRequest request) {

        return kycService.reviewKyc(clientId, request);
    }
}
