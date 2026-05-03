package com.akaslan.common.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
    String orderId,
    String customerId,
    BigDecimal totalPrice,
    List<OrderItemEventDto> items,
    PaymentInfoEventDto paymentInfo
) {}
