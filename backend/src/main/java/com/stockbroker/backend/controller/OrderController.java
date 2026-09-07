package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.OrderRequest;
import com.stockbroker.backend.dto.OrderResponse;
import com.stockbroker.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place Order - Appendix A: Client/Dealer/Admin only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','DEALER','ADMIN')")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }

    /**
     * Get Order By ID - ownership enforced in OrderServiceImpl.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Get Orders By Client - ownership enforced in OrderServiceImpl.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByClient(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(
                orderService.getOrdersByClient(clientId));
    }

    /**
     * Get Order Book - Appendix A: Dealer/Compliance Officer(L)/Risk
     * Manager/Admin.
     */
    @GetMapping("/book")
    @PreAuthorize("hasAnyRole('DEALER','COMPLIANCE_OFFICER','RISK_MANAGER','ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrderBook() {

        return ResponseEntity.ok(
                orderService.getAllOrders());
    }

    /**
     * Cancel Order - ownership enforced in OrderServiceImpl.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long id) {

        orderService.cancelOrder(id);

        return ResponseEntity.ok("Order Cancelled Successfully");
    }
}
