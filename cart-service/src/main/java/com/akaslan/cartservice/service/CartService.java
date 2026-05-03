package com.akaslan.cartservice.service;

import com.akaslan.cartservice.dto.CartRequest;
import com.akaslan.cartservice.model.Cart;

public interface CartService {
    Cart getCart(String userId);
    Cart addToCart(String userId, CartRequest request);
    Cart removeFromCart(String userId, Long productId);
    Cart decrementItem(String userId, Long productId);
    void clearCart(String userId);
}
