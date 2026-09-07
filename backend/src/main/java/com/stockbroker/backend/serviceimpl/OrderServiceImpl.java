package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.OrderRequest;
import com.stockbroker.backend.dto.OrderResponse;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.Stock;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.NotificationType;
import com.stockbroker.backend.enums.OrderSide;
import com.stockbroker.backend.enums.OrderStatus;
import com.stockbroker.backend.enums.OrderType;
import com.stockbroker.backend.exception.DuplicateTradeException;
import com.stockbroker.backend.exception.InsufficientHoldingsException;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.exception.TradingNotAllowedException;
import com.stockbroker.backend.repository.OrderRepository;
import com.stockbroker.backend.repository.StockRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.AuditLogService;
import com.stockbroker.backend.service.KycService;
import com.stockbroker.backend.service.LedgerService;
import com.stockbroker.backend.service.MarginService;
import com.stockbroker.backend.service.NotificationService;
import com.stockbroker.backend.service.OrderService;
import com.stockbroker.backend.service.PortfolioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The order execution engine (SRS FR4). See the approved implementation
 * plan for the full design; key simplifications documented inline:
 *  - LIMIT orders execute at the limit price itself once the market
 *    condition is met (no partial fills / price improvement modeling).
 *  - STOP_LOSS/BRACKET/COVER stop legs execute at the prevailing market
 *    price once triggered.
 *  - BRACKET/COVER are only supported on the BUY side (no short-selling in
 *    this cash/delivery-equity system) - their SELL-side exit legs
 *    (target/stop-loss) are auto-created as child orders once the entry
 *    leg executes, and are OCO (one executing cancels the other).
 *  - Pending SELL orders do not reserve/lock the underlying holdings, so a
 *    client could in principle place multiple pending sells whose total
 *    exceeds current holdings; each is still re-checked against current
 *    holdings at execution time, so a stale sell will fail then rather
 *    than over-selling. Documented simplification, not a real oversell risk.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final int DUPLICATE_WINDOW_SECONDS = 5;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final KycService kycService;
    private final PortfolioService portfolioService;
    private final MarginService marginService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            StockRepository stockRepository,
                            KycService kycService,
                            PortfolioService portfolioService,
                            MarginService marginService,
                            LedgerService ledgerService,
                            NotificationService notificationService,
                            AuditLogService auditLogService) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.kycService = kycService;
        this.portfolioService = portfolioService;
        this.marginService = marginService;
        this.ledgerService = ledgerService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        kycService.assertTradingAllowed(client.getId());

        String symbol = request.getStockSymbol().toUpperCase();

        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + symbol));

        if (stock.isCircuitHalted()) {
            throw new TradingNotAllowedException(
                    symbol + " is currently in a circuit halt - new orders are not accepted");
        }

        validateOrderShape(request);

        if ((request.getOrderType() == OrderType.BRACKET || request.getOrderType() == OrderType.COVER)
                && request.getOrderSide() == OrderSide.SELL) {
            throw new IllegalArgumentException(
                    "BRACKET/COVER orders are only supported on the BUY side in this system (no short-selling)");
        }

        LocalDateTime duplicateWindowStart =
                LocalDateTime.now().minusSeconds(DUPLICATE_WINDOW_SECONDS);

        List<Order> possibleDuplicates = orderRepository
                .findByClientIdAndStockSymbolAndOrderSideAndQuantityAndPriceAndPlacedAtAfter(
                        client.getId(), symbol, request.getOrderSide(),
                        request.getQuantity(), request.getPrice(), duplicateWindowStart);

        if (!possibleDuplicates.isEmpty()) {
            throw new DuplicateTradeException(
                    "An identical order was just submitted - please wait a moment before retrying");
        }

        double estimatedPrice = request.getPrice() != null ? request.getPrice()
                : (request.getTriggerPrice() != null ? request.getTriggerPrice() : stock.getCurrentPrice());
        double estimatedAmount = request.getQuantity() * estimatedPrice;

        if (request.getOrderSide() == OrderSide.BUY) {
            marginService.ensureSufficientMargin(client.getId(), estimatedAmount);
        } else {
            if (!portfolioService.hasSufficientHoldings(client.getId(), symbol, request.getQuantity())) {
                throw new InsufficientHoldingsException(
                        "Insufficient holdings of " + symbol + " to place this sell order");
            }
        }

        Order order = new Order();
        order.setStockSymbol(symbol);
        order.setCompanyName(request.getCompanyName());
        order.setOrderType(request.getOrderType());
        order.setOrderSide(request.getOrderSide());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setTriggerPrice(request.getTriggerPrice());
        order.setTargetPrice(request.getTargetPrice());
        order.setTotalAmount(estimatedAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setClient(client);

        Order saved = orderRepository.save(order);

        auditLogService.record("ORDER_PLACED", "Order", saved.getId().toString(),
                saved.getOrderSide() + " " + saved.getQuantity() + " " + saved.getStockSymbol());

        if (isImmediatelyExecutable(saved, stock)) {
            executeOrder(saved, resolveExecutionPrice(saved, stock));
        }

        return mapToResponse(orderRepository.findById(saved.getId()).orElse(saved));
    }

    private void validateOrderShape(OrderRequest request) {

        switch (request.getOrderType()) {

            case LIMIT -> {
                if (request.getPrice() == null || request.getPrice() <= 0) {
                    throw new IllegalArgumentException("LIMIT orders require a positive price");
                }
            }
            case STOP_LOSS -> {
                if (request.getTriggerPrice() == null || request.getTriggerPrice() <= 0) {
                    throw new IllegalArgumentException("STOP_LOSS orders require a positive triggerPrice");
                }
            }
            case BRACKET -> {
                if (request.getTriggerPrice() == null || request.getTargetPrice() == null) {
                    throw new IllegalArgumentException(
                            "BRACKET orders require both triggerPrice (stop) and targetPrice");
                }
            }
            case COVER -> {
                if (request.getTriggerPrice() == null) {
                    throw new IllegalArgumentException("COVER orders require a triggerPrice (stop)");
                }
            }
            case MARKET -> {
                // no extra fields required
            }
        }
    }

    private boolean isImmediatelyExecutable(Order order, Stock stock) {

        if (order.getOrderType() == OrderType.STOP_LOSS) {
            return false;
        }

        boolean marketLike = order.getOrderType() == OrderType.MARKET
                || ((order.getOrderType() == OrderType.BRACKET || order.getOrderType() == OrderType.COVER)
                    && order.getPrice() == null);

        if (marketLike) {
            return true;
        }

        // LIMIT-like (LIMIT, or BRACKET/COVER with an entry limit price)
        if (order.getPrice() != null) {
            return order.getOrderSide() == OrderSide.BUY
                    ? stock.getCurrentPrice() <= order.getPrice()
                    : stock.getCurrentPrice() >= order.getPrice();
        }

        return false;
    }

    private double resolveExecutionPrice(Order order, Stock stock) {
        return order.getPrice() != null ? order.getPrice() : stock.getCurrentPrice();
    }

    /**
     * Applies the effects of an execution: portfolio/FIFO lots, ledger
     * settlement, margin recalculation, notification, and - for a BRACKET/
     * COVER entry leg - auto-creation of the linked exit order(s).
     */
    @Transactional
    protected void executeOrder(Order order, double executionPrice) {

        order.setPrice(executionPrice);
        order.setTotalAmount(order.getQuantity() * executionPrice);
        order.setStatus(OrderStatus.EXECUTED);
        order.setExecutedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (order.getOrderSide() == OrderSide.BUY) {
            portfolioService.applyBuy(order.getClient(), order.getStockSymbol(),
                    order.getCompanyName(), order.getQuantity(), executionPrice, order);
        } else {
            portfolioService.applySell(order.getClient(), order.getStockSymbol(),
                    order.getQuantity(), executionPrice, order.getExecutedAt());
        }

        ledgerService.recordTradeSettlement(order);
        marginService.recalculate(order.getClient().getId());

        String message = String.format("Your %s order for %d x %s executed at %.2f",
                order.getOrderSide(), order.getQuantity(), order.getStockSymbol(), executionPrice);

        notificationService.notify(order.getClient(), NotificationType.ORDER_EXECUTED, message);

        if (order.getParentOrder() != null) {
            cancelSiblingOrders(order);
        }

        if ((order.getOrderType() == OrderType.BRACKET || order.getOrderType() == OrderType.COVER)
                && order.getOrderSide() == OrderSide.BUY
                && order.getParentOrder() == null) {
            createExitOrders(order);
        }
    }

    private void createExitOrders(Order entry) {

        if (entry.getOrderType() == OrderType.BRACKET && entry.getTargetPrice() != null) {

            Order target = new Order();
            target.setStockSymbol(entry.getStockSymbol());
            target.setCompanyName(entry.getCompanyName());
            target.setOrderType(OrderType.LIMIT);
            target.setOrderSide(OrderSide.SELL);
            target.setQuantity(entry.getQuantity());
            target.setPrice(entry.getTargetPrice());
            target.setTotalAmount(entry.getQuantity() * entry.getTargetPrice());
            target.setClient(entry.getClient());
            target.setParentOrder(entry);

            orderRepository.save(target);
        }

        if (entry.getTriggerPrice() != null) {

            Order stop = new Order();
            stop.setStockSymbol(entry.getStockSymbol());
            stop.setCompanyName(entry.getCompanyName());
            stop.setOrderType(OrderType.STOP_LOSS);
            stop.setOrderSide(OrderSide.SELL);
            stop.setQuantity(entry.getQuantity());
            stop.setTriggerPrice(entry.getTriggerPrice());
            stop.setTotalAmount(entry.getQuantity() * entry.getTriggerPrice());
            stop.setClient(entry.getClient());
            stop.setParentOrder(entry);

            orderRepository.save(stop);
        }
    }

    private void cancelSiblingOrders(Order justExecuted) {

        List<Order> siblings = orderRepository.findByParentOrderIdAndStatus(
                justExecuted.getParentOrder().getId(), OrderStatus.PENDING);

        for (Order sibling : siblings) {
            if (!sibling.getId().equals(justExecuted.getId())) {
                sibling.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(sibling);
            }
        }
    }

    @Override
    @Transactional
    public void tryExecutePendingOrders() {

        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pending) {

            Stock stock = stockRepository.findBySymbol(order.getStockSymbol()).orElse(null);

            if (stock == null || stock.isCircuitHalted()) {
                continue;
            }

            Double executionPrice = resolveTriggerExecutionPrice(order, stock);

            if (executionPrice == null) {
                continue;
            }

            if (order.getOrderSide() == OrderSide.SELL
                    && !portfolioService.hasSufficientHoldings(
                            order.getClient().getId(), order.getStockSymbol(), order.getQuantity())) {
                continue;
            }

            executeOrder(order, executionPrice);
        }
    }

    /**
     * @return the execution price if order's trigger condition is
     *         currently met, or null if it should keep waiting.
     */
    private Double resolveTriggerExecutionPrice(Order order, Stock stock) {

        double currentPrice = stock.getCurrentPrice();

        return switch (order.getOrderType()) {

            case LIMIT -> {
                boolean met = order.getOrderSide() == OrderSide.BUY
                        ? currentPrice <= order.getPrice()
                        : currentPrice >= order.getPrice();
                yield met ? order.getPrice() : null;
            }

            case STOP_LOSS -> {
                boolean met = order.getOrderSide() == OrderSide.BUY
                        ? currentPrice >= order.getTriggerPrice()
                        : currentPrice <= order.getTriggerPrice();
                yield met ? currentPrice : null;
            }

            default -> null; // MARKET/BRACKET/COVER entries execute immediately at placement
        };
    }

    @Override
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id : " + id));

        SecurityUtils.assertCanAccessClient(order.getClient().getId());

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByClient(Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);

        return orderRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id : " + id));

        SecurityUtils.assertCanAccessClient(order.getClient().getId());

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be cancelled - executed orders are immutable");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        marginService.recalculate(order.getClient().getId());

        auditLogService.record("ORDER_CANCELLED", "Order", order.getId().toString(), null);
    }

    private OrderResponse mapToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setStockSymbol(order.getStockSymbol());
        response.setCompanyName(order.getCompanyName());
        response.setOrderType(order.getOrderType());
        response.setOrderSide(order.getOrderSide());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setTriggerPrice(order.getTriggerPrice());
        response.setTargetPrice(order.getTargetPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPlacedAt(order.getPlacedAt());
        response.setExecutedAt(order.getExecutedAt());

        response.setClientId(order.getClient().getId());

        response.setClientName(
                order.getClient().getFirstName() + " "
                        + order.getClient().getLastName());

        return response;
    }
}
