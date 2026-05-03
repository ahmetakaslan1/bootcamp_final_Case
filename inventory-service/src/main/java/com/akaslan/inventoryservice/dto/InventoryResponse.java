package com.akaslan.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ürünün güncel stok durumunu içeren yanıt modeli")
public record InventoryResponse(
    @Schema(description = "Ürün ID'si", example = "10")
    Long productId,
    @Schema(description = "Güncel stok adedi", example = "45")
    Integer quantity,
    @Schema(description = "Stokta olup olmadığı", example = "true")
    boolean isInStock
) {}
