package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.entity.ClientAccount;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.enums.TradingStatus;
import com.stockbroker.backend.repository.ClientAccountRepository;
import com.stockbroker.backend.repository.OrderRepository;
import com.stockbroker.backend.service.ComplianceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generates the regulatory export files described in SRS FR8. There is no
 * reachable SEBI upload portal in this environment, so these return the
 * generated CSV content directly rather than submitting it anywhere.
 */
@Service
public class ComplianceServiceImpl implements ComplianceService {

    private final OrderRepository orderRepository;
    private final ClientAccountRepository clientAccountRepository;

    public ComplianceServiceImpl(OrderRepository orderRepository,
                                  ClientAccountRepository clientAccountRepository) {
        this.orderRepository = orderRepository;
        this.clientAccountRepository = clientAccountRepository;
    }

    @Override
    public String generateDailyActivityReportCsv(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Order> executed = orderRepository.findByExecutedAtBetween(start, end);

        StringBuilder csv = new StringBuilder(
                "OrderId,ClientId,Symbol,Side,OrderType,Quantity,Price,TotalAmount,ExecutedAt\n");

        for (Order order : executed) {

            csv.append(order.getId()).append(',')
                    .append(order.getClient().getId()).append(',')
                    .append(order.getStockSymbol()).append(',')
                    .append(order.getOrderSide()).append(',')
                    .append(order.getOrderType()).append(',')
                    .append(order.getQuantity()).append(',')
                    .append(order.getPrice() != null ? order.getPrice() : "").append(',')
                    .append(order.getTotalAmount()).append(',')
                    .append(order.getExecutedAt()).append('\n');
        }

        return csv.toString();
    }

    @Override
    public String generateUccFileCsv() {

        List<ClientAccount> accounts = clientAccountRepository.findAll();

        StringBuilder csv = new StringBuilder("ClientId,ClientName,TradingStatus,KycStatus\n");

        for (ClientAccount account : accounts) {

            if (account.getTradingStatus() != TradingStatus.ACTIVE) {
                continue;
            }

            csv.append(account.getClient().getId()).append(',')
                    .append(account.getClient().getFirstName()).append(' ')
                    .append(account.getClient().getLastName()).append(',')
                    .append(account.getTradingStatus()).append(',')
                    .append(account.getKycStatus()).append('\n');
        }

        return csv.toString();
    }
}
