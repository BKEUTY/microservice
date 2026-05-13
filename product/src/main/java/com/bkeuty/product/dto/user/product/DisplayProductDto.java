package com.bkeuty.product.dto.user.product;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayProductDto {
    private Integer productId;
    private String variantName;
    private BigDecimal originPrice;
    private BigDecimal discountPrice;
    private String appliedPromotionType;
    private String imageUrl;
    private Integer stockQuantity;
    private Integer sold;
    private String brand;
    private List<CategoryDto> categories;
    private String status;
    private String description;
    private Double averageRating;
    private Integer reviewCount;
}
