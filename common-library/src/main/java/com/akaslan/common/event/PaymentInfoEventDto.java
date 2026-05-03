package com.akaslan.common.event;

public record PaymentInfoEventDto(
    String cardNumber,
    String expireMonth,
    String expireYear,
    String cvc,
    String cardHolderName
) {}
