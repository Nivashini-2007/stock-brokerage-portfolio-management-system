package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.OrderRequest;
import com.stockbroker.backend.dto.OrderResponse;
import com.stockbroker.backend.entity.Order;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.OrderStatus;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.OrderRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found"));

        Order order = new Order();

        order.setStockSymbol(request.getStockSymbol());
        order.setCompanyName(request.getCompanyName());
        order.setOrderType(request.getOrderType());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());

        order.setTotalAmount(
                request.getQuantity() * request.getPrice()
        );

        order.setStatus(OrderStatus.PENDING);
        order.setClient(client);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id : " + id));

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByClient(Long clientId) {

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
    public void cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id : " + id));

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setStockSymbol(order.getStockSymbol());
        response.setCompanyName(order.getCompanyName());
        response.setOrderType(order.getOrderType());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());

        response.setClientId(order.getClient().getId());

        response.setClientName(
                order.getClient().getFirstName() + " "
                        + order.getClient().getLastName());

        return response;
    }
}