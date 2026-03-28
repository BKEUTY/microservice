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
public class ProductDetailDto {
    private Integer id;
    private String name;
    private String description;
    private String image;
    private BigDecimal originPrice;
    private BigDecimal promotionPrice;
    private String brand;
    private List<ProductVariantDto> variants;
    private List<ProductOptionDto> options;
    private List<CategoryDto> categories;
}
