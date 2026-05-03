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

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(jwt.getSubject(), request);
        return ResponseEntity.ok(ApiResponse.success("Sipariş başarıyla oluşturuldu", orderResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByCustomer(jwt.getSubject())));
    }
}
