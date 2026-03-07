package com.bkeuty.product.dto.admin;

import com.bkeuty.product.enums.ProductStatus;
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
public class AdminProductVariantDto {
    private Integer id;
    private BigDecimal price;
    private String productVariantName;
    private Integer stockQuantity;
    private String description;
    private String productImageUrl;
    private List<String> optionValues;
    private ProductStatus status;
    private String productName;
}
