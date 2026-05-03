package com.akaslan.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sipariş oluşturma isteği için gerekli olan teslimat ve ödeme bilgileri")
public record OrderRequest(
    @Schema(description = "Teslimat adresi", example = "Atatürk Mah. Cumhuriyet Cad. No:1")
    @NotBlank(message = "{order.address.notblank}")
    String shippingAddress,

    @Schema(description = "Alıcı Adı Soyadı", example = "Ahmet Akaslan")
    @NotBlank(message = "Alıcı adı boş olamaz")
    String receiverName,

    @Schema(description = "Alıcı Telefon Numarası", example = "05551234567")
    @NotBlank(message = "Telefon numarası boş olamaz")
    String phoneNumber,
    
    @Schema(description = "Kredi kartı numarası (16 haneli)", example = "4545123412341234")
    @NotBlank(message = "{order.cardnumber.notblank}")
    String cardNumber,
    
    @Schema(description = "Kredi kartı son kullanma ayı", example = "12")
    @NotBlank(message = "{order.expiremonth.notblank}")
    String expireMonth,
    
    @Schema(description = "Kredi kartı son kullanma yılı", example = "2028")
    @NotBlank(message = "{order.expireyear.notblank}")
    String expireYear,
    
    @Schema(description = "Kredi kartı güvenlik kodu (CVC)", example = "123")
    @NotBlank(message = "{order.cvc.notblank}")
    String cvc,
    
    @Schema(description = "Kredi kartı sahibinin adı soyadı", example = "Ahmet Akaslan")
    @NotBlank(message = "{order.cardholder.notblank}")
    String cardHolderName
) {}
