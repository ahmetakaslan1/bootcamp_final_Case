package com.akaslan.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Yeni ürün oluşturma veya güncelleme isteği modeli")
public record ProductRequest(
    @Schema(description = "Ürünün vitrinde görünecek adı", example = "MacBook Pro 14")
    @NotBlank(message = "{product.name.notblank}") 
    String name,
    
    @Schema(description = "Ürünün detaylı açıklaması", example = "M3 Pro çipli en son nesil MacBook")
    String description,
    
    @Schema(description = "Ürünün satış fiyatı", example = "75000.00")
    @NotNull(message = "{product.price.notnull}") 
    @DecimalMin(value = "0.0", inclusive = false, message = "{product.price.min}") 
    BigDecimal price,
    
    @Schema(description = "Ürünün kategorisi", example = "Elektronik")
    String category,
    
    @Schema(description = "Ürünün vitrin görsel URL'si", example = "https://example.com/macbook.jpg")
    String imageUrl
) {}
