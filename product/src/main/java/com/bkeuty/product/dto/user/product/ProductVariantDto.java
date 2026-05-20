package com.bkeuty.product.dto.user.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantDto {
    private Integer id;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stockQuantity;
    private Integer sold;
    private List<String> productImageUrl;
    private String productVariantName;
    private Map<String, String> variantOptions;
}
