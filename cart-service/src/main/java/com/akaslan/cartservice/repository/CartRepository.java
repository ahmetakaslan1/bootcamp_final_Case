package com.akaslan.cartservice.repository;

import com.akaslan.cartservice.model.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {
}
