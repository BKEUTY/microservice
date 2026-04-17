package com.bkeuty.order.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProductPerformanceDto {
    private LocalDateTime date;
    private Integer productId;
    private String productVariantName;
    private Long quantity;
    private BigDecimal revenue;
    private BigDecimal profit;

    public DailyProductPerformanceDto(LocalDateTime date, Integer productId, String productVariantName, Long quantity, BigDecimal revenue) {
        this.date = date;
        this.productId = productId;
        this.productVariantName = productVariantName;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }
}
