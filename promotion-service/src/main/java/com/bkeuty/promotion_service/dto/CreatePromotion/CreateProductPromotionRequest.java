package com.bkeuty.promotion_service.dto.CreatePromotion;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateProductPromotionRequest extends CreatePromotionRequest {


    // Child fields
    private Set<Integer> categoryIds;
    private Set<Integer> productIds;
    private Set<Integer> brandIds;
}
