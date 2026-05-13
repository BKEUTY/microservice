package com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass;

import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class CreatePromotionResponse {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String type;
    private DiscountType discountType;
    private PromotionStatus status;
    private Integer discountValue;
    private Integer maxDiscountValue;
    private java.util.Set<Integer> membershipLevels;
}
