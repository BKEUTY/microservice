package com.bkeuty.product.dto.user.product;

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
public class DisplayProductDto {
    private Integer productId;
    private String variantName;
    private BigDecimal originPrice;
    private BigDecimal discountPrice;
    private String imageUrl;
    private Integer stock;
    private String brand;
    private List<CategoryDto> categories;
}
