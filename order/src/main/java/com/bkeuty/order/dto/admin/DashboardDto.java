package com.bkeuty.order.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private Overview overview;
    private PerformanceAggregationResponseDto topPerformers;
    private List<ChartDataDto> revenueChart;
    private List<DashboardOrderDto> recentOrders;
    private List<TopCustomerDto> topCustomers;
    private List<DailyProductPerformanceDto> productDetail;
    private List<TransactionalPerformanceDto> brandDetail;
    private List<TransactionalPerformanceDto> categoryDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private Long totalProductsSold;
        private Double productsSoldGrowth;
        private Long totalRegisteredCustomers;
        private Double customersGrowth;
        private Long totalOrders;
        private Double ordersGrowth;
        private BigDecimal totalRevenue;
        private Double revenueGrowth;
        private BigDecimal totalShippingFee;
        private BigDecimal totalProfit;
    }
}
