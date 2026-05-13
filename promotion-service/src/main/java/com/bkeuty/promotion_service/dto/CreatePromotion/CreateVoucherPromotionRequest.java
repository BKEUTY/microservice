package com.bkeuty.promotion_service.dto.CreatePromotion;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateVoucherPromotionRequest extends CreatePromotionRequest {
    private String code;
    private Integer totalQuantity;
    private BigDecimal minOrderValue;
    private Integer usageLimitPerUser;
}
