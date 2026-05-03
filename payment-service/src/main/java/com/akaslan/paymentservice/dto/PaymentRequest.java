package com.akaslan.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
    @NotBlank(message = "{payment.orderid.notblank}") String orderId,
    @NotNull(message = "{payment.amount.notnull}") BigDecimal amount,
    @NotBlank(message = "{payment.cardnumber.notblank}") String cardNumber,
    @NotBlank(message = "{payment.expiremonth.notblank}") String expireMonth,
    @NotBlank(message = "{payment.expireyear.notblank}") String expireYear,
    @NotBlank(message = "{payment.cvc.notblank}") String cvc,
    @NotBlank(message = "{payment.cardholder.notblank}") String cardHolderName
) {}
