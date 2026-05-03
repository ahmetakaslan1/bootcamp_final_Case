package com.akaslan.productservice.client;

import com.akaslan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", path = "/api/v1/inventory")
public interface InventoryClient {

    @GetMapping("/{productId}")
    ApiResponse<InventoryResponse> getStock(@PathVariable("productId") Long productId);
}
