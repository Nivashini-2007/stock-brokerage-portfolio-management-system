package com.stockbroker.backend.service.kyc;

/**
 * Boundary interface for PAN/DEMAT/bank verification. The real
 * implementations (Income Tax PAN API, CDSL/NSDL DEMAT lookup, bank penny-
 * drop) are unreachable in this environment, so MockKycVerificationProvider
 * is registered instead - swap in a real implementation of this interface
 * to go live without touching KycServiceImpl.
 */
public interface KycVerificationProvider {

    boolean verifyPan(String panNumber, String firstName, String lastName);

    boolean verifyDemat(String demateId);

    boolean verifyBankAccount(String accountNumber, String ifsc);
}
