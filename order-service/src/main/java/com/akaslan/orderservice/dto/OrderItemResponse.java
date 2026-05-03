package com.akaslan.orderservice.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Siparişteki her bir ürünün detay modeli")
public record OrderItemResponse(
    @Schema(description = "Satın alınan ürünün ID'si", example = "10")
    Long productId,
    @Schema(description = "Satın alınan adet", example = "2")
    Integer quantity,
    @Schema(description = "Ürünün satın alındığı anki birim fiyatı", example = "75000.00")
    BigDecimal price
) {}
