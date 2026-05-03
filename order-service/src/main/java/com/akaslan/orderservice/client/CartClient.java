package com.akaslan.orderservice.client;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.orderservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", path = "/api/v1/carts", configuration = FeignConfig.class)
public interface CartClient {

    @GetMapping
    ApiResponse<CartResponse> getMyCart();

    @DeleteMapping
    ApiResponse<Void> clearCart();

    @DeleteMapping("/internal/{userId}")
    ApiResponse<Void> clearCartByUserId(@PathVariable("userId") String userId);
}
