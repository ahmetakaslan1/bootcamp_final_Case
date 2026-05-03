package com.akaslan.orderservice.dto;

import com.akaslan.orderservice.entity.Order;
import com.akaslan.orderservice.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Sipariş detaylarını içeren yanıt modeli")
public record OrderResponse(
    @Schema(description = "Siparişin veritabanındaki benzersiz kimliği", example = "1")
    Long id,
    @Schema(description = "Müşteriye gösterilen benzersiz sipariş takip numarası", example = "ORD-ABC123XYZ")
    String orderNumber,
    @Schema(description = "Siparişin toplam tutarı", example = "1500.50")
    BigDecimal totalPrice,
    @Schema(description = "Siparişin anlık durumu (Örn: PENDING, COMPLETED, FAILED)")
    OrderStatus status,
    @Schema(description = "Teslimat Adresi")
    String shippingAddress,
    @Schema(description = "Alıcı Adı")
    String receiverName,
    @Schema(description = "Alıcı Telefonu")
    String phoneNumber,
    @Schema(description = "Eğer ödeme başarısız olduysa hatanın nedeni", example = "Yetersiz bakiye")
    String paymentFailureReason,
    @Schema(description = "Siparişin oluşturulma tarihi")
    LocalDateTime createdAt,
    @Schema(description = "Siparişteki ürünlerin listesi")
    List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getShippingAddress(),
            order.getReceiverName(),
            order.getPhoneNumber(),
            order.getPaymentFailureReason(),
            order.getCreatedAt(),
            order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList()
        );
    }
}
