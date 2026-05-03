package com.akaslan.orderservice.service;

import com.akaslan.orderservice.dto.OrderRequest;
import com.akaslan.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String customerId, OrderRequest request);
    List<OrderResponse> getOrdersByCustomer(String customerId);
}
