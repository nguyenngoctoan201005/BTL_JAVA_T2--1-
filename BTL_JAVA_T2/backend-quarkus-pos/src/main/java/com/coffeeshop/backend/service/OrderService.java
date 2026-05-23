package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.order.CreateOrderRequest;
import com.coffeeshop.backend.dto.order.OrderResponse;
import com.coffeeshop.backend.enums.OrderStatus;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, String userEmail);


    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);




    boolean isOwnerOfOrder(Long orderId, String username);

    Page<OrderResponse> getOrdersByUserId(String username, Pageable pageable);

    Page<OrderResponse> getAllOrders(java.security.Principal principal, Pageable pageable);

    void cancelOrder(Long orderId, String username);

    void processMockPayment(Long orderId);
}
