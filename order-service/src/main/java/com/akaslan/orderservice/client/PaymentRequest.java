package com.akaslan.orderservice.client;

import java.math.BigDecimal;

public record PaymentRequest(
    String orderId,
    BigDecimal amount,
    String cardNumber,
    String expireMonth,
    String expireYear,
    String cvc,
    String cardHolderName
) {}
