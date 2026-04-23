package com.bkeuty.product.dto.user.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDataRequestDto {
    private Set<Integer> productIds;
    private Set<Integer> categoryIds;
    private Set<Integer> brandIds;
}
