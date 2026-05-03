package com.akaslan.orderservice.repository;

import com.akaslan.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(String customerId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
