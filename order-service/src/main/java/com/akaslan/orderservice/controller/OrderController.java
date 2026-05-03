package com.akaslan.orderservice.controller;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.orderservice.dto.OrderRequest;
import com.akaslan.orderservice.dto.OrderResponse;
import com.akaslan.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Sipariş Yönetimi", description = "Müşterilerin sipariş oluşturma ve görüntüleme işlemleri")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Sipariş Oluştur", description = "Müşterinin sepetindeki ürünler için sipariş oluşturur ve ödeme sürecini başlatır")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(jwt.getSubject(), request);
        return ResponseEntity.ok(ApiResponse.success("Sipariş başarıyla oluşturuldu", orderResponse));
    }

    @GetMapping
    @Operation(summary = "Siparişlerimi Getir", description = "Giriş yapmış müşterinin geçmiş tüm siparişlerini listeler")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByCustomer(jwt.getSubject())));
    }
}
