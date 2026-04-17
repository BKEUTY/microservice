package com.bkeuty.product.dto.internal;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResultDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("productVariantName")
    private String productVariantName;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("quantity")
    private Long quantity;
    
    @JsonProperty("revenue")
    private BigDecimal revenue;

    @JsonProperty("profit")
    private BigDecimal profit;

    public PerformanceResultDto(Integer id, String productVariantName, String imageUrl, Long quantity, BigDecimal revenue) {
        this.id = id;
        this.productVariantName = productVariantName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }
}
