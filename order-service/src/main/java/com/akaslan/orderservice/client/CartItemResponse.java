package com.akaslan.orderservice.client;

import java.math.BigDecimal;

public record CartItemResponse(
    Long productId,
    Integer quantity,
    BigDecimal price
) {}
