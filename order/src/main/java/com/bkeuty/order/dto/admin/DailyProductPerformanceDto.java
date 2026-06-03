package com.bkeuty.order.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProductPerformanceDto {
    private LocalDateTime date;
    private Integer variantId;
    private String name;
    private Long quantity;
    private BigDecimal revenue;
    private BigDecimal profit;
    private BigDecimal originalPrice;
    private BigDecimal promotionalPrice;
    private BigDecimal voucherDiscount;
    private Boolean isRefunded;

    public DailyProductPerformanceDto(LocalDateTime date, Integer variantId, String name, Long quantity, BigDecimal revenue, BigDecimal originalPrice, BigDecimal promotionalPrice, BigDecimal voucherDiscount, Boolean isRefunded) {
        this.date = date;
        this.variantId = variantId;
        this.name = name;
        this.quantity = quantity;
        this.isRefunded = isRefunded;
        this.revenue = Boolean.TRUE.equals(isRefunded) ? BigDecimal.ZERO : (revenue != null ? revenue : BigDecimal.ZERO);
        this.profit = Boolean.TRUE.equals(isRefunded) ? BigDecimal.ZERO : (revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO);
        this.originalPrice = originalPrice;
        this.promotionalPrice = promotionalPrice;
        this.voucherDiscount = voucherDiscount;
    }
}
