package com.bkeuty.product.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantPerformanceDto {
    private Integer variantId;
    private Long quantity;
    private BigDecimal revenue;
}
