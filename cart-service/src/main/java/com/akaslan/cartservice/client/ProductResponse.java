package com.akaslan.cartservice.client;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    Integer stockQuantity,
    boolean isInStock
) {}
