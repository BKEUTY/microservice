package com.bkeuty.promotion_service.dto.CreatePromotion;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class CreateVoucherPromotionResponse extends CreatePromotionResponse {
    private String code;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private BigDecimal minOrderValue;
    private Integer usageLimitPerUser;
}
