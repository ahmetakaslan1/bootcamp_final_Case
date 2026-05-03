package com.akaslan.cartservice.controller;

import com.akaslan.cartservice.dto.CartRequest;
import com.akaslan.cartservice.model.Cart;
import com.akaslan.cartservice.service.CartService;
import com.akaslan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Sepet Yönetimi", description = "Kullanıcıların alışveriş sepeti işlemleri")
public class CartController {

    private final CartService cartService;
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    @Value("${integration.cart.internal-api-key:local-dev-internal-key}")
    private String internalApiKey;

    @GetMapping
    @Operation(summary = "Sepetimi Getir", description = "Giriş yapmış kullanıcının sepetini getirir")
    public ResponseEntity<ApiResponse<Cart>> getMyCart(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(jwt.getSubject())));
    }

    @PostMapping("/items")
    @Operation(summary = "Sepete Ürün Ekle", description = "Kullanıcının sepetine ürün ekler veya var olan ürünün sayısını artırır")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ürün sepete eklendi", cartService.addToCart(jwt.getSubject(), request)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Sepetten Ürün Sil", description = "Belirtilen ürünü sepetten tamamen çıkarır")
    public ResponseEntity<ApiResponse<Cart>> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Ürün sepetten çıkarıldı", cartService.removeFromCart(jwt.getSubject(), productId)));
    }

    @PatchMapping("/items/{productId}/decrement")
    @Operation(summary = "Ürün Adedini Azalt", description = "Sepetteki ürünün adedini 1 azaltır. Sayı 0 olursa ürünü sepetten siler")
    public ResponseEntity<ApiResponse<Cart>> decrementItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Ürün adedi azaltıldı", cartService.decrementItem(jwt.getSubject(), productId)));
    }
 

    @DeleteMapping
    @Operation(summary = "Sepeti Boşalt", description = "Kullanıcının sepetindeki tüm ürünleri siler")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Sepet temizlendi"));
    }

    @DeleteMapping("/internal/{userId}")
    @Operation(summary = "Sistem Tarafından Sepet Boşalt (Internal)", description = "Sipariş tamamlandığında backend servislerinin sepeti temizlemesi için kullanılır")
    public ResponseEntity<ApiResponse<Void>> clearCartForUser(
            @PathVariable String userId,
            @RequestHeader(name = INTERNAL_API_KEY_HEADER, required = false) String requestInternalApiKey) {
        if (requestInternalApiKey == null || !requestInternalApiKey.equals(internalApiKey)) {
            throw new AccessDeniedException("Geçersiz internal api key");
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Sepet sistem tarafından temizlendi"));
    }
}
