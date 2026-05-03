package com.akaslan.orderservice.dto;

import com.akaslan.orderservice.entity.Order;
import com.akaslan.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    BigDecimal totalPrice,
    OrderStatus status,
    String paymentFailureReason,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getPaymentFailureReason(),
            order.getCreatedAt(),
            order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList()
        );
    }
}
