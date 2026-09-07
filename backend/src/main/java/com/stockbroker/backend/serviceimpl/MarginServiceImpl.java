package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.config.TradingProperties;
import com.stockbroker.backend.dto.MarginResponse;
import com.stockbroker.backend.entity.Margin;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.NotificationType;
import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.OrderStatus;
import com.stockbroker.backend.exception.InsufficientMarginException;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.MarginRepository;
import com.stockbroker.backend.repository.OrderRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.LedgerService;
import com.stockbroker.backend.service.MarginService;
import com.stockbroker.backend.service.NotificationService;
import com.stockbroker.backend.service.RiskAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pragmatic cash/delivery-equity margin model (see plan §4): no F&O
 * leverage exists in this system, so:
 *   totalMargin     = client's ledger cash balance
 *   usedMargin      = value of the client's own PENDING BUY orders
 *                      (funds earmarked awaiting execution)
 *   availableMargin = totalMargin - usedMargin
 *
 * Margin call at >=80% utilization raises a risk alert + notification.
 * Auto square-off at >=90% cancels the client's own oldest PENDING BUY
 * orders (never force-sells settled holdings) until utilization drops
 * below the threshold - a deliberately conservative simplification of
 * "position square-off" for a demo system.
 */
@Service
public class MarginServiceImpl implements MarginService {

    private final MarginRepository marginRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;
    private final RiskAlertService riskAlertService;
    private final NotificationService notificationService;
    private final TradingProperties tradingProperties;

    public MarginServiceImpl(MarginRepository marginRepository,
                              OrderRepository orderRepository,
                              UserRepository userRepository,
                              LedgerService ledgerService,
                              RiskAlertService riskAlertService,
                              NotificationService notificationService,
                              TradingProperties tradingProperties) {

        this.marginRepository = marginRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.ledgerService = ledgerService;
        this.riskAlertService = riskAlertService;
        this.notificationService = notificationService;
        this.tradingProperties = tradingProperties;
    }

    @Override
    public MarginResponse getMargin(Long clientId) {

        Margin margin = marginRepository.findByClientId(clientId)
                .orElseGet(() -> createDefaultMargin(clientId));

        MarginResponse response = new MarginResponse();

        response.setClientId(margin.getClient().getId());
        response.setClientName(
                margin.getClient().getFirstName() + " " +
                margin.getClient().getLastName());

        response.setAvailableMargin(margin.getAvailableMargin());
        response.setUsedMargin(margin.getUsedMargin());
        response.setTotalMargin(margin.getTotalMargin());

        response.setUtilizationPercentage(utilization(margin) * 100);

        return response;
    }

    @Override
    @Transactional
    public void recalculate(Long clientId) {

        Margin margin = marginRepository.findByClientId(clientId)
                .orElseGet(() -> createDefaultMargin(clientId));

        double totalMargin = ledgerService.getCurrentBalance(clientId);

        List<Order> pendingBuysAsc = orderRepository
                .findByClientIdAndStatusOrderByPlacedAtAsc(clientId, OrderStatus.PENDING)
                .stream()
                .filter(o -> o.getOrderSide() == OrderSide.BUY)
                .collect(Collectors.toList());

        double usedMargin = pendingBuysAsc.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        margin.setTotalMargin(totalMargin);
        margin.setUsedMargin(usedMargin);
        margin.setAvailableMargin(totalMargin - usedMargin);
        marginRepository.save(margin);

        double utilization = utilization(margin);

        if (utilization >= tradingProperties.getSquareOffThreshold()) {
            squareOff(margin, pendingBuysAsc);
        } else if (utilization >= tradingProperties.getMarginCallThreshold()) {

            String message = String.format(
                    "Margin utilization is at %.1f%%. Please add funds to avoid an automatic position square-off at %.0f%%.",
                    utilization * 100, tradingProperties.getSquareOffThreshold() * 100);

            riskAlertService.createAlert(margin.getClient(), "HIGH", "Margin Call", message);
            notificationService.notify(margin.getClient(), NotificationType.MARGIN_CALL, message);
        }
    }

    private void squareOff(Margin margin, List<Order> pendingBuysAsc) {

        double totalMargin = margin.getTotalMargin();
        double usedMargin = margin.getUsedMargin();

        for (Order order : pendingBuysAsc) {

            double currentUtilization = totalMargin > 0
                    ? usedMargin / totalMargin
                    : (usedMargin > 0 ? 1.0 : 0.0);

            if (currentUtilization < tradingProperties.getSquareOffThreshold()) {
                break;
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            usedMargin -= order.getTotalAmount();

            String message = String.format(
                    "Pending %s order #%d for %s (qty %d) was automatically cancelled to release blocked margin after utilization reached the %.0f%% square-off threshold.",
                    order.getOrderSide(), order.getId(), order.getStockSymbol(),
                    order.getQuantity(), tradingProperties.getSquareOffThreshold() * 100);

            riskAlertService.createAlert(margin.getClient(), "CRITICAL", "Position Square-Off", message);
            notificationService.notify(margin.getClient(), NotificationType.POSITION_SQUARE_OFF, message);
        }

        margin.setUsedMargin(Math.max(usedMargin, 0));
        margin.setAvailableMargin(totalMargin - margin.getUsedMargin());
        marginRepository.save(margin);
    }

    @Override
    public void ensureSufficientMargin(Long clientId, double orderValue) {

        recalculate(clientId);

        Margin margin = marginRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Margin details not found for client : " + clientId));

        if (margin.getAvailableMargin() < orderValue) {

            throw new InsufficientMarginException(String.format(
                    "Insufficient margin: available %.2f is less than the required %.2f",
                    margin.getAvailableMargin(), orderValue));
        }
    }

    private double utilization(Margin margin) {

        if (margin.getTotalMargin() == null || margin.getTotalMargin() <= 0) {
            return margin.getUsedMargin() != null && margin.getUsedMargin() > 0 ? 1.0 : 0.0;
        }

        return margin.getUsedMargin() / margin.getTotalMargin();
    }

    private Margin createDefaultMargin(Long clientId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Margin margin = new Margin();
        margin.setClient(client);
        margin.setAvailableMargin(0.0);
        margin.setUsedMargin(0.0);
        margin.setTotalMargin(0.0);

        return marginRepository.save(margin);
    }
}
