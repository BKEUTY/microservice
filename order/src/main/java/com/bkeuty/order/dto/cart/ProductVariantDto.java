package com.bkeuty.order.dto.cart;

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
    private BigDecimal promotionPrice;
    private String productImageUrl;
    private String productVariantName;
    private String productVariantDescription;
    private Integer stockQuantity;
}
