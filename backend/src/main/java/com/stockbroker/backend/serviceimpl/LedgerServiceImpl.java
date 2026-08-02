package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.LedgerResponse;
import com.stockbroker.backend.dto.TransferRequest;
import com.stockbroker.backend.entity.Ledger;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.TransactionType;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.LedgerRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.LedgerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    public LedgerServiceImpl(LedgerRepository ledgerRepository,
                             UserRepository userRepository) {
        this.ledgerRepository = ledgerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<LedgerResponse> getLedger(Long clientId) {

        return ledgerRepository
                .findByClientIdOrderByTransactionDateDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LedgerResponse transferFunds(TransferRequest request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found"));

        double currentBalance = 0.0;

        List<Ledger> entries =
                ledgerRepository.findByClientIdOrderByTransactionDateDesc(
                        client.getId());

        if (!entries.isEmpty()) {
            currentBalance = entries.get(0).getBalance();
        }

        double newBalance;

        if (request.getTransactionType() == TransactionType.DEPOSIT) {

            newBalance = currentBalance + request.getAmount();

        } else if (request.getTransactionType() == TransactionType.WITHDRAWAL) {

            if (currentBalance < request.getAmount()) {
                throw new RuntimeException("Insufficient balance");
            }

            newBalance = currentBalance - request.getAmount();

        } else {

            throw new RuntimeException(
                    "Only DEPOSIT and WITHDRAWAL are allowed");
        }

        Ledger ledger = new Ledger();

        ledger.setClient(client);
        ledger.setTransactionType(request.getTransactionType());
        ledger.setAmount(request.getAmount());
        ledger.setBalance(newBalance);

        if (request.getTransactionType() == TransactionType.DEPOSIT) {
            ledger.setDescription("Amount Deposited");
        } else {
            ledger.setDescription("Amount Withdrawn");
        }

        Ledger savedLedger = ledgerRepository.save(ledger);

        return mapToResponse(savedLedger);
    }

    private LedgerResponse mapToResponse(Ledger ledger) {

        LedgerResponse response = new LedgerResponse();

        response.setId(ledger.getId());
        response.setTransactionType(ledger.getTransactionType());
        response.setDescription(ledger.getDescription());
        response.setAmount(ledger.getAmount());
        response.setBalance(ledger.getBalance());
        response.setTransactionDate(ledger.getTransactionDate());

        response.setClientId(ledger.getClient().getId());

        response.setClientName(
                ledger.getClient().getFirstName() + " "
                        + ledger.getClient().getLastName());

        return response;
    }
}