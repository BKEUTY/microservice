package com.bkeuty.promotion_service.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionCheckResponseDTO {
    @com.fasterxml.jackson.annotation.JsonProperty("newPrice")
    private BigDecimal newPrice;
    @com.fasterxml.jackson.annotation.JsonProperty("appliedPromotionType")
    private String appliedPromotionType; 
}
