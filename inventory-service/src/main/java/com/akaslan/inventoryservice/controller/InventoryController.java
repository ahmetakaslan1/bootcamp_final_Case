package com.akaslan.inventoryservice.controller;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.inventoryservice.dto.InventoryRequest;
import com.akaslan.inventoryservice.dto.InventoryResponse;
import com.akaslan.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Stok Yönetimi", description = "Ürünlerin stok adetlerini izleme ve güncelleme işlemleri")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Stok Durumunu Getir", description = "Belirtilen ürünün depodaki güncel stok adedini döner")
    public ResponseEntity<ApiResponse<InventoryResponse>> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProductId(productId)));
    }

    @PostMapping
    @Operation(summary = "Stok Güncelle", description = "Ürünün depo stok sayısını manuel olarak günceller")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stok güncellendi", inventoryService.updateInventory(request)));
    }
    // deduct = indirim , düşmek
    @PostMapping("/deduct")
    @Operation(summary = "Stok Düş", description = "Sipariş oluşturulduğunda ürünün stok adedini eksiltir")
    public ResponseEntity<ApiResponse<Void>> deductStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        inventoryService.deductInventory(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stok düşüldü"));
    }
}
