package com.akaslan.common.event;

import java.math.BigDecimal;

public record InventoryDeductedEvent(
    String orderId,
    String customerId,
    BigDecimal totalPrice,
    PaymentInfoEventDto paymentInfo
) {}
