package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.config.TradingProperties;
import com.stockbroker.backend.dto.LedgerResponse;
import com.stockbroker.backend.dto.TransferRequest;
import com.stockbroker.backend.entity.Ledger;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.TradeSettlement;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.TransactionType;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.LedgerRepository;
import com.stockbroker.backend.repository.TradeSettlementRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;
    private final TradeSettlementRepository tradeSettlementRepository;
    private final TradingProperties tradingProperties;

    public LedgerServiceImpl(LedgerRepository ledgerRepository,
                             UserRepository userRepository,
                             TradeSettlementRepository tradeSettlementRepository,
                             TradingProperties tradingProperties) {
        this.ledgerRepository = ledgerRepository;
        this.userRepository = userRepository;
        this.tradeSettlementRepository = tradeSettlementRepository;
        this.tradingProperties = tradingProperties;
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
    public double getCurrentBalance(Long clientId) {

        List<Ledger> entries =
                ledgerRepository.findByClientIdOrderByTransactionDateDesc(clientId);

        return entries.isEmpty() ? 0.0 : entries.get(0).getBalance();
    }

    @Override
    public LedgerResponse transferFunds(TransferRequest request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found"));

        double currentBalance = getCurrentBalance(client.getId());

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

    @Override
    @Transactional
    public void recordTradeSettlement(Order order) {

        double grossAmount = order.getTotalAmount();

        double brokerage = round(grossAmount * tradingProperties.getBrokerageRate());
        double gst = round(brokerage * tradingProperties.getGstRate());
        double stt = round(grossAmount * tradingProperties.getSttRate());
        double exchangeCharges = round(grossAmount * tradingProperties.getExchangeChargeRate());
        double stampDuty = order.getOrderSide() == OrderSide.BUY
                ? round(grossAmount * tradingProperties.getStampDutyRate())
                : 0.0;

        double totalCharges = brokerage + gst + stt + exchangeCharges + stampDuty;

        // BUY debits cash (gross + charges); SELL credits cash (gross - charges).
        double netAmount = order.getOrderSide() == OrderSide.BUY
                ? -(grossAmount + totalCharges)
                : (grossAmount - totalCharges);

        double currentBalance = getCurrentBalance(order.getClient().getId());
        double newBalance = currentBalance + netAmount;

        Ledger ledger = new Ledger();
        ledger.setClient(order.getClient());
        ledger.setTransactionType(order.getOrderSide() == OrderSide.BUY
                ? TransactionType.BUY_ORDER
                : TransactionType.SELL_ORDER);
        ledger.setAmount(netAmount);
        ledger.setBalance(newBalance);
        ledger.setDescription(String.format(
                "%s %d x %s @ %.2f (brokerage %.2f, GST %.2f, STT %.2f, exch %.2f, stamp %.2f)",
                order.getOrderSide(), order.getQuantity(), order.getStockSymbol(),
                order.getPrice() != null ? order.getPrice() : 0.0,
                brokerage, gst, stt, exchangeCharges, stampDuty));

        ledgerRepository.save(ledger);

        TradeSettlement settlement = new TradeSettlement();
        settlement.setOrder(order);
        settlement.setGrossAmount(grossAmount);
        settlement.setBrokerage(brokerage);
        settlement.setGst(gst);
        settlement.setStt(stt);
        settlement.setExchangeCharges(exchangeCharges);
        settlement.setStampDuty(stampDuty);
        settlement.setNetAmount(netAmount);
        settlement.setTradeDate(LocalDate.now());
        settlement.setSettlementDate(LocalDate.now().plusDays(1));
        settlement.setSettled(false);

        tradeSettlementRepository.save(settlement);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
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
