package com.bkeuty.product.dto.user.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPromotionDto {
    private Integer productVariantId;
    private Integer productId;
    private List<Integer> categoryIds;
    private Integer brandId;
    private BigDecimal price;
}
