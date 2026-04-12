package com.bkeuty.product.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {
    private String recommendation;
    private List<DisplayProductDto> recommendedProducts;
    private long timestamp;
}
