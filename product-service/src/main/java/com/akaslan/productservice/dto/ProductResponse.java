package com.akaslan.productservice.dto;

import com.akaslan.productservice.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    String category,
    String imageUrl,
    Integer stockQuantity,
    boolean isInStock,
    LocalDateTime createdAt,
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
