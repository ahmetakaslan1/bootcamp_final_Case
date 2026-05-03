package com.akaslan.inventoryservice.controller;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.inventoryservice.dto.InventoryRequest;
import com.akaslan.inventoryservice.dto.InventoryResponse;
import com.akaslan.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProductId(productId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stok güncellendi", inventoryService.updateInventory(request)));
    }
    // deduct = indirim , düşmek
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<Void>> deductStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        inventoryService.deductInventory(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stok düşüldü"));
    }
}
