package com.akaslan.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stok miktarını güncellemek için istek modeli")
public record InventoryRequest(
    @Schema(description = "Stok durumu güncellenecek ürünün ID'si", example = "10")
    @NotNull(message = "{inventory.productId.notnull}") 
    Long productId,

    @Schema(description = "Yeni stok adedi", example = "50")
    @NotNull(message = "{inventory.quantity.notnull}") 
    @Min(value = 0, message = "{inventory.quantity.min}")
    Integer quantity
) {}
