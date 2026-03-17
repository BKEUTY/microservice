package com.bkeuty.promotion_service.dto.CreatePromotion;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class CreateProductPromotionResponse extends CreatePromotionResponse {


    // Child fields
    private Set<Integer> categoryIds;
    private Set<Integer> productIds;
    private Set<Integer> brandIds;
}
