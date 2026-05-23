package com.coffeeshop.backend.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private String couponCode;
    private String deliveryMethod;
    private Long addressId;
    private Long storeId;
}
