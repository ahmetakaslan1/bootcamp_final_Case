package com.akaslan.inventoryservice.service;

import com.akaslan.inventoryservice.dto.InventoryRequest;
import com.akaslan.inventoryservice.dto.InventoryResponse;

public interface InventoryService {
    InventoryResponse getInventoryByProductId(Long productId);
    InventoryResponse updateInventory(InventoryRequest request);
    void deductInventory(Long productId, Integer quantityToDeduct);
    void restoreInventory(Long productId, Integer quantityToRestore);
}
