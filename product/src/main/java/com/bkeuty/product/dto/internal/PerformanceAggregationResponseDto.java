package com.bkeuty.product.dto.internal;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAggregationResponseDto {
    @JsonProperty("topProducts")
    private List<PerformanceResultDto> topProducts;
    
    @JsonProperty("topBrands")
    private List<PerformanceResultDto> topBrands;
    
    @JsonProperty("topCategories")
    private List<PerformanceResultDto> topCategories;
    
    @JsonProperty("variantMappings")
    private java.util.Map<Integer, VariantMappingDto> variantMappings;
}
