package com.akaslan.cartservice.service.impl;

import com.akaslan.cartservice.client.ProductClient;
import com.akaslan.cartservice.client.ProductResponse;
import com.akaslan.cartservice.dto.CartRequest;
import com.akaslan.cartservice.model.Cart;
import com.akaslan.cartservice.model.CartItem;
import com.akaslan.cartservice.repository.CartRepository;
import com.akaslan.cartservice.service.CartService;
import com.akaslan.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Override
    public Cart getCart(String userId) {
        return cartRepository.findById(userId)
            .orElseGet(() -> Cart.builder().id(userId).build());
    }

    @Override
    public Cart addToCart(String userId, CartRequest request) {
        Cart cart = getCart(userId);

 
        ProductResponse product = null;
        try {
            var response = productClient.getProductById(request.productId());
            if (response != null) {
                product = response.getData();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ürün servisi ile iletişim kurulamadı. Hata: " + e.getMessage());
        }

        if (product == null) {
            throw new ResourceNotFoundException("Product", request.productId());
        }

        // 2. Stok kontrolü
        if (!product.isInStock() || (product.stockQuantity() != null && product.stockQuantity() < request.quantity())) {
            throw new IllegalArgumentException("Bu ürün için yeterli stok bulunmuyor!");
        }

        // 3. Sepette bu ürün var mı diye bak, varsa miktarını artır
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(request.productId()))
            .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.quantity());
            existingItem.get().setPrice(product.price()); // Sepetteki fiyatı her zaman güncele çek
        } else {
            // Sepette yoksa yeni ekle
            cart.getItems().add(CartItem.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .price(product.price())
                .build());
        }

        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeFromCart(String userId, Long productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public Cart decrementItem(String userId, Long productId) {
        Cart cart = getCart(userId);

        cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                if (item.getQuantity() <= 1) {
                    cart.getItems().removeIf(existingItem -> existingItem.getProductId().equals(productId));
                } else {
                    item.setQuantity(item.getQuantity() - 1);
                }
            });

        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.deleteById(userId);
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }
}
