package com.akaslan.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartRequest(
    @NotNull(message = "{cart.productId.notnull}") 
    Long productId,

    @NotNull(message = "{cart.quantity.notnull}") 
    @Min(value = 1, message = "{cart.quantity.min}")
    Integer quantity
) {}
