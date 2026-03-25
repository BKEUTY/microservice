package com.bkeuty.product.dto.user.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartProductVariantDto {
    private Integer id;
    private BigDecimal price;
    private String productImageUrl;
    private String productVariantName;
    private String productVariantDescription;
    private BigDecimal promotionPrice;
}
