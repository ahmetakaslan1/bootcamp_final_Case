package com.akaslan.productservice.dto;

import com.akaslan.productservice.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ürün detaylarını ve stok durumunu içeren yanıt modeli")
public record ProductResponse(
    @Schema(description = "Ürünün benzersiz ID'si", example = "10")
    Long id,
    @Schema(description = "Ürünün vitrinde görünecek adı", example = "MacBook Pro 14")
    String name,
    @Schema(description = "Ürünün detaylı açıklaması")
    String description,
    @Schema(description = "Ürünün anlık fiyatı", example = "75000.00")
    BigDecimal price,
    @Schema(description = "Ürünün kategorisi", example = "Elektronik")
    String category,
    @Schema(description = "Ürünün görsel URL'si")
    String imageUrl,
    @Schema(description = "Güncel stok miktarı (Liste sorgularında null dönebilir)", example = "5")
    Integer stockQuantity,
    @Schema(description = "Ürün şu an stokta var mı?", example = "true")
    boolean isInStock,
    @Schema(description = "Sisteme eklenme tarihi")
    LocalDateTime createdAt,
    @Schema(description = "Son güncellenme tarihi")
    LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product, Integer stockQuantity, boolean isInStock) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getImageUrl(),
            stockQuantity,
            isInStock,
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    // Geriye dönük uyumluluk (findAll gibi liste metodları için stoğu null/false dönmek performansı artırır)
    public static ProductResponse from(Product product) {
        return from(product, null, false);
    }
}
