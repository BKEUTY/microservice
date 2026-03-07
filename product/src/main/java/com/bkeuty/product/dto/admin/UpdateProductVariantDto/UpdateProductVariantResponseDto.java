package com.bkeuty.product.dto.admin.UpdateProductVariantDto;

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
public class UpdateProductVariantResponseDto {
    private Integer id;
    private BigDecimal price = null;
    private String productVariantName = null;
    private Integer stockQuantity = null;
    private String description = null;
    private String productImageUrl = null;
    private List<String> optionValues = null;
    private ProductStatus status = null;
    private String productName = null;
}
