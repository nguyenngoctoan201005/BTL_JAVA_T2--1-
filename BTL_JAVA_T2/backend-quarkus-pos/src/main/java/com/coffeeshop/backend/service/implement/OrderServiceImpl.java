package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.dto.order.CreateOrderRequest;
import com.coffeeshop.backend.dto.order.OrderItemRequest;
import com.coffeeshop.backend.dto.order.OrderResponse;
import com.coffeeshop.backend.entity.*;
import com.coffeeshop.backend.enums.OrderStatus;
import com.coffeeshop.backend.enums.PaymentMethod;
import com.coffeeshop.backend.enums.PaymentStatus;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.mapper.OrderMapper;
import com.coffeeshop.backend.repository.*;
import com.coffeeshop.backend.service.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.lang.SecurityException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.coffeeshop.backend.service.ShippingService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;
    private final ProductStockRepository productStockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final ShippingService shippingService;
    private final OrderMapper orderMapper;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void processMockPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(OrderStatus.PAID);

        Payment payment = order.getPayment();
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found for order id: " + orderId);
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);
        orderRepository.save(order);

    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        // 1. Find the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // 1.5. Find the store
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getStoreId()));

        // 2. Create a new Order
        Order order = new Order();
        order.setUser(user);
        order.setStore(store);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDetails(new ArrayList<>());

        // 3. Process order items and calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ProductVariant not found with id: " + itemRequest.getProductVariantId()));

            // ProductStock productStock =
            // productStockRepository.findAndLockByProductVariantIdAndStoreId(variant.getId(),
            // store.getId())
            // .orElseThrow(() -> new RuntimeException("Product not available at this
            // store"));

            // 1. Tìm kho, nếu không có thì trả về null
            ProductStock productStock = productStockRepository
                    .findAndLockByProductVariantIdAndStoreId(variant.getId(), store.getId())
                    .orElse(null);

            // 2. HACK: Tự động "bơm" 100 sản phẩm vào kho nếu hệ thống quên tạo
            if (productStock == null) {
                productStock = new ProductStock();
                productStock.setProductVariant(variant);
                productStock.setStore(store);
                productStock.setQuantity(100);
                productStock = productStockRepository.save(productStock);
            }

            if (productStock.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for product: " + variant.getProduct().getName() + " - " + variant.getSize());
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductVariant(variant);
            detail.setQuantity(itemRequest.getQuantity());
            detail.setUnitPrice(variant.getPrice()); // Use price from the database

            order.getOrderDetails().add(detail);

            subtotal = subtotal.add(variant.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));

            // Decrease stock and create history
            int newQuantity = productStock.getQuantity() - itemRequest.getQuantity();
            productStock.setQuantity(newQuantity);
            productStockRepository.save(productStock);

            StockHistory history = new StockHistory();
            history.setProductVariant(variant);
            history.setStore(store);
            history.setQuantityChanged(-itemRequest.getQuantity());
            history.setCurrentQuantity(newQuantity);
            history.setReason("SALE");
            history.setCreatedBy(user.getId());
            stockHistoryRepository.save(history);
        }

        // 4. No voucher support
        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);

        // 5. Calculate VAT
        BigDecimal vat = subtotalAfterDiscount.multiply(new BigDecimal("0.08"));

        // 6. Calculate shipping fee
        BigDecimal shippingFee = BigDecimal.ZERO;
        if ("delivery".equalsIgnoreCase(request.getDeliveryMethod())) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            com.coffeeshop.backend.dto.ShippingInfoDTO shippingInfo = shippingService
                    .calculateShippingFeeForStore(store.getId(), address.getLatitude(), address.getLongitude());
            shippingFee = shippingInfo.getShippingFee();
        }

        BigDecimal totalPrice = subtotalAfterDiscount.add(vat).add(shippingFee);

        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        order.setTotalPrice(totalPrice);

        // 7. Create and associate Payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.VNPAY); // Default or from request
        payment.setStatus(PaymentStatus.PENDING);
        order.setPayment(payment);

        // 8. Save the order
        Order savedOrder = orderRepository.save(order);

        try {
            // log.info("Đang gọi cổng thanh toán (Mock External API)...");
            String paymentMockUrl = "https://webhook.site/b5975016-ca13-49c7-ac8e-f4d1dbceae17";
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(paymentMockUrl)
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // log.info("Gọi cổng thanh toán thành công!");
        } catch (Exception e) {
            // log.error("Lỗi khi gọi mock payment: ", e);
        }

        // 9. Map to response DTO
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        if (status != OrderStatus.DELIVERED) {
            // WebSocket broadcast removed
        }

        return orderMapper.toOrderResponse(updatedOrder);
    }

    public boolean isOwnerOfOrder(Long orderId, String username) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getEmail().equals(username))
                .orElse(false);
    }

    @Override
    public Page<OrderResponse> getOrdersByUserId(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<Order> orders = orderRepository.findAllByUserId(user.getId(), pageable);

        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(java.security.Principal principal, Pageable pageable) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + principal.getName()));

        Page<Order> orders;
        if (user.getRole() == com.coffeeshop.backend.enums.UserRole.ADMIN) {
            orders = orderRepository.findAll(pageable);
        } else if (user.getRole() == com.coffeeshop.backend.enums.UserRole.STAFF) {
            if (user.getStore() == null) {
                throw new SecurityException("Staff user is not assigned to any store.");
            }
            orders = orderRepository.findByStoreId(user.getStore().getId(), pageable);
        } else {
            throw new SecurityException("You do not have permission to view all orders.");
        }

        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Verify ownership
        if (!order.getUser().getEmail().equals(username)) {
            throw new SecurityException("You are not authorized to cancel this order.");
        }

        // Check if order is in a cancellable state
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order cannot be cancelled once it is being prepared.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Broadcast the status update (WebSocket removed)
    }
}
