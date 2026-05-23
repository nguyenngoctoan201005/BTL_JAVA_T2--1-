package com.coffeeshop.backend.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Long productVariantId; // Use variant ID for accuracy
    private int quantity;
    private BigDecimal price; // Price at the time of adding to cart
}