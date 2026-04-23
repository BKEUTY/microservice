package com.bkeuty.product.dto.user.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDataResponseDto {
    private Map<Integer, String> productNames;
    private Map<Integer, String> categoryNames;
    private Map<Integer, String> brandNames;
}
