package com.bkeuty.product.dto.user.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecreaseStockResponseDto {
    private Integer productVariantId;
    private String productVariantName;
    private String productVariantImage;
    private Integer quantity;
    private BigDecimal price;
}
