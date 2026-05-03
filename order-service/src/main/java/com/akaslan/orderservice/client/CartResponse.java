package com.akaslan.orderservice.client;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    String id,
    List<CartItemResponse> items,
    BigDecimal totalPrice
) {}
