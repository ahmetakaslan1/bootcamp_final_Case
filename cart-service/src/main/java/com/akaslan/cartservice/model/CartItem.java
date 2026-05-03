package com.akaslan.cartservice.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private Long productId;
    private Integer quantity;
    private BigDecimal price; 
}
