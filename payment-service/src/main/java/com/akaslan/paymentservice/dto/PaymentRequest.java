package com.akaslan.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kredi kartından ödeme çekmek için gerekli olan model")
public record PaymentRequest(
    @Schema(description = "İlişkili siparişin numarası", example = "ORD-123")
    @NotBlank(message = "{payment.orderid.notblank}") String orderId,
    @Schema(description = "Çekilecek toplam tutar", example = "150.00")
    @NotNull(message = "{payment.amount.notnull}") BigDecimal amount,
    @Schema(description = "Kredi kartı numarası (16 haneli)", example = "4545123412341234")
    @NotBlank(message = "{payment.cardnumber.notblank}") String cardNumber,
    @Schema(description = "Kart son kullanma ayı", example = "12")
    @NotBlank(message = "{payment.expiremonth.notblank}") String expireMonth,
    @Schema(description = "Kart son kullanma yılı", example = "2028")
    @NotBlank(message = "{payment.expireyear.notblank}") String expireYear,
    @Schema(description = "Kart güvenlik kodu (CVC)", example = "123")
    @NotBlank(message = "{payment.cvc.notblank}") String cvc,
    @Schema(description = "Kart sahibinin adı soyadı", example = "Ahmet Akaslan")
    @NotBlank(message = "{payment.cardholder.notblank}") String cardHolderName
) {}
