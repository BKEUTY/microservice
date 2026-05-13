package com.bkeuty.promotion_service.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceCalculationResult {
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private Integer appliedPromotionId;
    private BigDecimal discountAmount;
}
