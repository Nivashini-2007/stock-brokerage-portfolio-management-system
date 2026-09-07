package com.stockbroker.backend.service.kyc;

import org.springframework.stereotype.Service;

/**
 * Mock stand-in for real PAN (Income Tax Dept.)/DEMAT (CDSL/NSDL)/bank
 * (penny-drop) verification APIs, none of which are reachable here. Does
 * format-only validation and auto-approves - a real deployment must swap
 * this out for a genuine KycVerificationProvider implementation before
 * going live.
 */
@Service
public class MockKycVerificationProvider implements KycVerificationProvider {

    @Override
    public boolean verifyPan(String panNumber, String firstName, String lastName) {
        return panNumber != null && panNumber.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    }

    @Override
    public boolean verifyDemat(String demateId) {
        return demateId != null && !demateId.isBlank();
    }

    @Override
    public boolean verifyBankAccount(String accountNumber, String ifsc) {
        return accountNumber != null && accountNumber.length() >= 6
                && ifsc != null && ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$");
    }
}
