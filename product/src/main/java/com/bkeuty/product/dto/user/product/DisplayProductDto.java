package com.bkeuty.product.dto.user.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayProductDto {
    private Integer productId;
    private String variantName;
    private BigDecimal originPrice;
    private BigDecimal discountPrice;
    private String imageUrl;
    private Integer stockQuantity;
    private String brand;
    private List<CategoryDto> categories;
    private String status;
    private String description;
    private Double averageRating;
    private Integer reviewCount;
}
