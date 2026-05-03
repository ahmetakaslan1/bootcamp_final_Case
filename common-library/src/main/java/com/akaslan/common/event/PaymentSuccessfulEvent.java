package com.akaslan.common.event;

public record PaymentSuccessfulEvent(
    String orderId,
    String customerId
) {}
