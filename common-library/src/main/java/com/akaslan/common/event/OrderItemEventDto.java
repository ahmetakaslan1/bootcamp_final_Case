package com.akaslan.common.event;

import java.math.BigDecimal;

public record OrderItemEventDto(
    Long productId,
    Integer quantity,
    BigDecimal price
) {}
