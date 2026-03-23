package com.bkeuty.product.dto.user.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PromotionPriceDto {
    private BigDecimal newPrice;
}
