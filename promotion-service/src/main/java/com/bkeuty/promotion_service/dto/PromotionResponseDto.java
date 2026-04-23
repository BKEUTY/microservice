package com.bkeuty.promotion_service.dto;

import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionResponseDto {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private PromotionStatus status;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer maxDiscount;
    private String promotionType;
    private Set<Integer> categoryIds;
    private Set<Integer> brandIds;
    private Set<Integer> productIds;
}
