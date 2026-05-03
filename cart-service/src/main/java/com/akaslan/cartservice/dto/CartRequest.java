package com.akaslan.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sepete eklenecek ürünün istek modeli")
public record CartRequest(
    @Schema(description = "Sepete eklenecek ürünün ID'si", example = "10")
    @NotNull(message = "{cart.productId.notnull}") 
    Long productId,

    @Schema(description = "Eklenecek ürünün adedi", example = "2")
    @NotNull(message = "{cart.quantity.notnull}") 
    @Min(value = 1, message = "{cart.quantity.min}")
    Integer quantity
) {}
