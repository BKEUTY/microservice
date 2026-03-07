package com.bkeuty.product.dto.user.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDto {
    private Integer id;
    private BigDecimal price;
    private String productImageUrl;
    private String productVariantName;
}
