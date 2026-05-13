package com.bkeuty.product.dto.user.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PromotionPriceDto {
    @com.fasterxml.jackson.annotation.JsonProperty("newPrice")
    private BigDecimal newPrice;
    @com.fasterxml.jackson.annotation.JsonProperty("appliedPromotionType")
    private String appliedPromotionType;

    public PromotionPriceDto(BigDecimal newPrice) {
        this.newPrice = newPrice;
        this.appliedPromotionType = null;
    }
}
