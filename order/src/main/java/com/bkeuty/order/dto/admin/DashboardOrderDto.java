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
public class DashboardOrderDto {
    private String id;
    private String customerName;
    private LocalDateTime date;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private String status;
}
