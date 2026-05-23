package com.coffeeshop.backend.mapper;

import com.coffeeshop.backend.dto.order.OrderItemResponse;
import com.coffeeshop.backend.dto.order.OrderResponse;
import com.coffeeshop.backend.dto.user.UserSummary;
import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderDetail;
import com.coffeeshop.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "orderDetails", target = "items")
    @Mapping(source = "totalPrice", target = "totalAmount")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.name", target = "productName")
    @Mapping(source = "productVariant.size", target = "size")
    OrderItemResponse toOrderItemResponse(OrderDetail orderDetail);

    UserSummary toUserSummary(User user);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderDetail> orderDetails);


    @Mapping(source = "orderDetails", target = "items")
    @Mapping(source = "totalPrice", target = "totalAmount")
    OrderResponse toOrderDTO(Order order);
}
