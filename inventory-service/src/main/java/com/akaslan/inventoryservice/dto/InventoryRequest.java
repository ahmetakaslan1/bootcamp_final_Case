package com.akaslan.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(
    @NotNull(message = "{inventory.productId.notnull}") 
    Long productId,

    @NotNull(message = "{inventory.quantity.notnull}") 
    @Min(value = 0, message = "{inventory.quantity.min}")
    Integer quantity
) {}
