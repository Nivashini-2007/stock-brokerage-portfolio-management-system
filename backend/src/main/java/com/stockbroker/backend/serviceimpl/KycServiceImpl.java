package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.ClientAccountResponse;
import com.stockbroker.backend.dto.KycReviewRequest;
import com.stockbroker.backend.dto.KycSubmitRequest;
import com.stockbroker.backend.entity.ClientAccount;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.KycStatus;
import com.stockbroker.backend.enums.NotificationType;
import com.stockbroker.backend.enums.TradingStatus;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.exception.TradingNotAllowedException;
import com.stockbroker.backend.repository.ClientAccountRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.AuditLogService;
import com.stockbroker.backend.service.KycService;
import com.stockbroker.backend.service.NotificationService;
import com.stockbroker.backend.service.kyc.KycVerificationProvider;
import org.springframework.stereotype.Service;

@Service
public class KycServiceImpl implements KycService {

    private final ClientAccountRepository clientAccountRepository;
    private final UserRepository userRepository;
    private final KycVerificationProvider verificationProvider;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public KycServiceImpl(ClientAccountRepository clientAccountRepository,
                           UserRepository userRepository,
                           KycVerificationProvider verificationProvider,
                           NotificationService notificationService,
                           AuditLogService auditLogService) {
        this.clientAccountRepository = clientAccountRepository;
        this.userRepository = userRepository;
        this.verificationProvider = verificationProvider;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    public ClientAccountResponse submitKyc(KycSubmitRequest request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        ClientAccount account = clientAccountRepository.findByClientId(client.getId())
                .orElseGet(() -> {
                    ClientAccount created = new ClientAccount();
                    created.setClient(client);
                    return created;
                });

        account.setPanNumber(request.getPanNumber());
        account.setDemateId(request.getDemateId());
        account.setBankAccountNumber(request.getBankAccountNumber());
        account.setBankIfsc(request.getBankIfsc());

        boolean panOk = verificationProvider.verifyPan(
                request.getPanNumber(), client.getFirstName(), client.getLastName());
        boolean demateOk = verificationProvider.verifyDemat(request.getDemateId());
        boolean bankOk = verificationProvider.verifyBankAccount(
                request.getBankAccountNumber(), request.getBankIfsc());

        if (panOk && demateOk && bankOk) {
            account.setKycStatus(KycStatus.VERIFIED);
            account.setTradingStatus(TradingStatus.ACTIVE);
        } else {
            account.setKycStatus(KycStatus.PENDING);
            account.setTradingStatus(TradingStatus.PENDING);
        }

        ClientAccount saved = clientAccountRepository.save(account);

        notificationService.notify(client, NotificationType.KYC_UPDATE,
                "Your KYC submission status: " + saved.getKycStatus());

        auditLogService.record("KYC_SUBMIT", "ClientAccount", saved.getId().toString(),
                "kycStatus=" + saved.getKycStatus());

        return mapToResponse(saved);
    }

    @Override
    public ClientAccountResponse reviewKyc(Long clientId, KycReviewRequest request) {

        ClientAccount account = clientAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No KYC submission found for client " + clientId));

        if (Boolean.TRUE.equals(request.getApproved())) {
            account.setKycStatus(KycStatus.VERIFIED);
            account.setTradingStatus(TradingStatus.ACTIVE);
        } else {
            account.setKycStatus(KycStatus.REJECTED);
            account.setTradingStatus(TradingStatus.SUSPENDED);
        }

        ClientAccount saved = clientAccountRepository.save(account);

        notificationService.notify(account.getClient(), NotificationType.KYC_UPDATE,
                "Your KYC has been " + saved.getKycStatus()
                        + (request.getRemarks() != null ? ": " + request.getRemarks() : ""));

        auditLogService.record("KYC_REVIEW", "ClientAccount", saved.getId().toString(),
                "kycStatus=" + saved.getKycStatus() + " remarks=" + request.getRemarks());

        return mapToResponse(saved);
    }

    @Override
    public ClientAccountResponse getClientAccount(Long clientId) {

        ClientAccount account = clientAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No KYC record found for client " + clientId));

        return mapToResponse(account);
    }

    /**
     * Used by OrderServiceImpl to enforce "PAN and verified DEMAT account
     * mandatory before any trade order is accepted" (SRS Appendix F).
     */
    @Override
    public void assertTradingAllowed(Long clientId) {

        ClientAccount account = clientAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new TradingNotAllowedException(
                        "Complete KYC submission before trading"));

        if (account.getKycStatus() != KycStatus.VERIFIED
                || account.getTradingStatus() != TradingStatus.ACTIVE) {

            throw new TradingNotAllowedException(
                    "Trading is not permitted: KYC status is " + account.getKycStatus()
                            + ", trading account status is " + account.getTradingStatus());
        }
    }

    private ClientAccountResponse mapToResponse(ClientAccount account) {

        ClientAccountResponse response = new ClientAccountResponse();

        response.setClientId(account.getClient().getId());
        response.setClientName(account.getClient().getFirstName() + " " + account.getClient().getLastName());
        response.setTradingStatus(account.getTradingStatus());
        response.setKycStatus(account.getKycStatus());
        response.setRiskProfile(account.getRiskProfile());

        String pan = account.getPanNumber();
        if (pan != null && pan.length() >= 4) {
            response.setMaskedPan("*".repeat(Math.max(0, pan.length() - 4)) + pan.substring(pan.length() - 4));
        }

        return response;
    }
}
