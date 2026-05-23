package com.coffeeshop.backend.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long productVariantId;
    private String productName;
    private String size;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
}
