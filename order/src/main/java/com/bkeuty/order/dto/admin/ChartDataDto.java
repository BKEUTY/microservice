package com.bkeuty.order.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orders;
    private BigDecimal profit;

    public ChartDataDto(LocalDate date, BigDecimal revenue, Long orders) {
        this.date = date;
        this.revenue = revenue;
        this.orders = orders;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }

    public ChartDataDto(LocalDateTime date, BigDecimal revenue, Long orders) {
        this.date = date != null ? date.toLocalDate() : null;
        this.revenue = revenue;
        this.orders = orders;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }

    public ChartDataDto(Date date, BigDecimal revenue, Long orders) {
        if (date != null) {
            if (date instanceof java.sql.Date sqlDate) {
                this.date = sqlDate.toLocalDate();
            } else {
                this.date = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        this.revenue = revenue;
        this.orders = orders;
        this.profit = revenue != null ? revenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;
    }
}
