package com.bkeuty.product.dto.admin.UpdateProductVariantDto;

import com.bkeuty.product.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductVariantRequestDto {
    private Integer id;
    private String productVariantName;
    private BigDecimal price = null;
    private Integer stockQuantity = null;
    private String description = null;
    private String productImageUrl = null;
    private ProductStatus status = null;
}
