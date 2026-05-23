package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/mock-payment-success")
    public ResponseEntity<?> mockPaymentSuccess(@RequestBody Map<String, Long> body) {
        Long orderId = body.get("orderId");
        logger.info("Processing mock payment for Order ID: {}", orderId);
        orderService.processMockPayment(orderId);
        return ResponseEntity.ok(Map.of("message", "Mock payment processed successfully"));
    }
}
