package com.akaslan.cartservice.client;

import com.akaslan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.akaslan.cartservice.config.FeignConfig;

@FeignClient(name = "product-service", path = "/api/v1/products", configuration = FeignConfig.class)
public interface ProductClient {

    @GetMapping("/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);
}
