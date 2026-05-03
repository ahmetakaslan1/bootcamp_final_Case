package com.akaslan.common.event;

public record PaymentFailedEvent(
    String orderId,
    String customerId,
    String reason
) {}
