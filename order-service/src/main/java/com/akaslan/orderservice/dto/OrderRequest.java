package com.akaslan.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
    @NotBlank(message = "{order.address.notblank}")
    String shippingAddress,
    
    @NotBlank(message = "{order.cardnumber.notblank}")
    String cardNumber,
    
    @NotBlank(message = "{order.expiremonth.notblank}")
    String expireMonth,
    
    @NotBlank(message = "{order.expireyear.notblank}")
    String expireYear,
    
    @NotBlank(message = "{order.cvc.notblank}")
    String cvc,
    
    @NotBlank(message = "{order.cardholder.notblank}")
    String cardHolderName
) {}
