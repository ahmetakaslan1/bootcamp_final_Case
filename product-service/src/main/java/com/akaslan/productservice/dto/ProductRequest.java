package com.akaslan.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "{product.name.notblank}") 
    String name,
    
    String description,
    
    @NotNull(message = "{product.price.notnull}") 
    @DecimalMin(value = "0.0", inclusive = false, message = "{product.price.min}") 
    BigDecimal price,
    
    String category,
    String imageUrl
) {}
