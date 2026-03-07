package com.bkeuty.order.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDto {
    private Integer cartId;
    private Integer productVariantId;
    private String name;
    private BigDecimal price;
    private String image;
    private Integer quantity;
}
