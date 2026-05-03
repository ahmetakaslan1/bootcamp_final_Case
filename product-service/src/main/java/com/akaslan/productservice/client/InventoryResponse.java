package com.akaslan.productservice.client;

public record InventoryResponse(
    Long productId,
    Integer quantity,
    boolean isInStock
) {}
