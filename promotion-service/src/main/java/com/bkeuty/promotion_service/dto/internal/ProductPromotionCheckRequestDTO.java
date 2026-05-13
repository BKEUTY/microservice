package com.bkeuty.promotion_service.dto.internal;

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
public class ProductPromotionCheckRequestDTO {
    private Integer productVariantId;
    private Integer productId;
    private List<Integer> categoryIds;
    private Integer brandId;
    private BigDecimal price;

    private String userId;
    private Integer membershipLevel;
}
