package com.akaslan.inventoryservice.dto;

public record InventoryResponse(
    Long productId,
    Integer quantity,
    boolean isInStock
) {}
