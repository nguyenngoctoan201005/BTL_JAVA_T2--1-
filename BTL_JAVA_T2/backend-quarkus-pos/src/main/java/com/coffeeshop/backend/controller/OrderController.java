package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.order.UpdateStatusRequest;
import com.coffeeshop.backend.enums.OrderStatus;
import com.coffeeshop.backend.dto.order.CreateOrderRequest;
import com.coffeeshop.backend.dto.order.OrderResponse;
import com.coffeeshop.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // TODO: replace with real authenticated user from Quarkus SecurityIdentity
        String email = "admin@gmail.com";
        OrderResponse response = orderService.createOrder(request, email);
        log.info("Order created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest statusRequest) {
        OrderStatus status = OrderStatus.valueOf(statusRequest.getStatus().toUpperCase());
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        // TODO: replace with real authenticated user from Quarkus SecurityIdentity
        String email = "admin@gmail.com";
        orderService.cancelOrder(orderId, email);
        return ResponseEntity.ok().build();
    }
}
