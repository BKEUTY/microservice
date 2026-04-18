package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.*;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final WebClient productWebClient;
    private final WebClient userWebClient;

    public AdminDashboardService(OrderRepository orderRepository,
                                 WebClient productWebClient,
                                 WebClient userWebClient) {
        this.orderRepository = orderRepository;
        this.productWebClient = productWebClient;
        this.userWebClient = userWebClient;
    }

    public DashboardDto getDashboardData(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : LocalDateTime.now();
        
        List<PaymentStatus> validStatuses = Arrays.asList(PaymentStatus.PAID, PaymentStatus.COMPLETED);

        Long totalOrders = orderRepository.countOrdersByDateRangeAndStatus(start, end, validStatuses);
        BigDecimal totalRevenue = orderRepository.sumRevenueByDateRangeAndStatus(start, end, validStatuses);
        Long totalProductsSold = orderRepository.sumProductsSoldByDateRangeAndStatus(start, end, validStatuses);

        Long startTimestamp = start != null ? start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
        Long endTimestamp = end != null ? end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;

        Long totalUsers = fetchUserCount(startTimestamp, endTimestamp, token);

        List<VariantPerformanceDto> variantPerformances = orderRepository.findVariantPerformanceByDateRangeAndStatus(start, end, validStatuses);
        PerformanceAggregationResponseDto analytics = fetchTopPerformers(variantPerformances, token);

        BigDecimal totalProfit = totalRevenue != null ? totalRevenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;

        DashboardDto.Overview overview = DashboardDto.Overview.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalProfit(totalProfit)
                .totalProductsSold(totalProductsSold != null ? totalProductsSold : 0L)
                .totalRegisteredCustomers(totalUsers != null ? totalUsers : 0L)
                .build();

        List<ChartDataDto> chartData = orderRepository.findRevenueChartDataByDateRange(start, end, validStatuses);
        
        // Detailed Transactional Report
        List<DailyProductPerformanceDto> productDetail = orderRepository.findDetailedItemPerformance(start, end, validStatuses);
        List<TransactionalPerformanceDto> brandDetail = new ArrayList<>();
        List<TransactionalPerformanceDto> categoryDetail = new ArrayList<>();

        if (analytics != null && productDetail != null) {
            Map<Integer, VariantMappingDto> mappings = analytics.getVariantMappings() != null 
                ? analytics.getVariantMappings() : Collections.emptyMap();
            
            productDetail.forEach(item -> {
                VariantMappingDto mapping = mappings.get(item.getVariantId());
                if (mapping != null) {
                    item.setName(mapping.getVariantName());
                    
                    // Brand Detail Entry
                    if (mapping.getBrandId() != null) {
                        brandDetail.add(new TransactionalPerformanceDto(
                            item.getDate(), mapping.getBrandId(), mapping.getBrandName(),
                            item.getVariantId(), mapping.getVariantName(), 
                            item.getQuantity(), item.getRevenue()
                        ));
                    }
                    
                    // Category Detail Entry
                    if (mapping.getCategoryId() != null) {
                        categoryDetail.add(new TransactionalPerformanceDto(
                            item.getDate(), mapping.getCategoryId(), mapping.getCategoryName(),
                            item.getVariantId(), mapping.getVariantName(), 
                            item.getQuantity(), item.getRevenue()
                        ));
                    }
                }
            });
        }

        List<TopCustomerDto> topCustomers = orderRepository.findTopCustomers(start, end, validStatuses, PageRequest.of(0, 10));
        List<DashboardOrderDto> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, 20));

        return DashboardDto.builder()
                .overview(overview)
                .topPerformers(analytics)
                .revenueChart(chartData)
                .topCustomers(topCustomers)
                .recentOrders(recentOrders)
                .productDetail(productDetail)
                .brandDetail(brandDetail)
                .categoryDetail(categoryDetail)
                .build();
    }

    public List<DashboardOrderDto> getDetailedOrders(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : LocalDateTime.now();
        List<PaymentStatus> validStatuses = Arrays.asList(PaymentStatus.PAID, PaymentStatus.COMPLETED);
        return orderRepository.findAllOrdersInDateRange(start, end, validStatuses);
    }

    public PerformanceAggregationResponseDto getDetailedProducts(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : LocalDateTime.now();
        List<PaymentStatus> validStatuses = Arrays.asList(PaymentStatus.PAID, PaymentStatus.COMPLETED);
        List<VariantPerformanceDto> variantPerformances = orderRepository.findVariantPerformanceByDateRangeAndStatus(start, end, validStatuses);
        return fetchTopPerformers(variantPerformances, token);
    }

    public List<TopCustomerDto> getDetailedCustomers(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : LocalDateTime.now();
        List<PaymentStatus> validStatuses = Arrays.asList(PaymentStatus.PAID, PaymentStatus.COMPLETED);
        return orderRepository.findTopCustomers(start, end, validStatuses, PageRequest.of(0, 100));
    }

    public List<UserDetailDto> getDetailedNewUsers(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : LocalDateTime.now();
        Long startTimestamp = start != null ? start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
        Long endTimestamp = end != null ? end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;

        try {
            return userWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/user/internal/list");
                        if (startTimestamp != null) builder.queryParam("startDate", startTimestamp);
                        if (endTimestamp != null) builder.queryParam("endDate", endTimestamp);
                        return builder.build();
                    })
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToFlux(UserDetailDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch detailed new users", e);
            return Collections.emptyList();
        }
    }

    private Long fetchUserCount(Long startTimestamp, Long endTimestamp, String token) {
        try {
            return userWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/user/internal/count");
                        if (startTimestamp != null) builder.queryParam("startDate", startTimestamp);
                        if (endTimestamp != null) builder.queryParam("endDate", endTimestamp);
                        return builder.build();
                    })
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch user count", e);
            return 0L;
        }
    }

    private PerformanceAggregationResponseDto fetchTopPerformers(List<VariantPerformanceDto> variantPerformances, String token) {
        if (variantPerformances == null || variantPerformances.isEmpty()) {
            return PerformanceAggregationResponseDto.builder()
                    .topProducts(Collections.emptyList())
                    .topBrands(Collections.emptyList())
                    .topCategories(Collections.emptyList())
                    .variantMappings(Collections.emptyMap())
                    .build();
        }
        try {
            PerformanceAggregationResponseDto response = productWebClient.post()
                    .uri("/api/product/internal/analytics/aggregate")
                    .header("Authorization", token)
                    .bodyValue(variantPerformances)
                    .retrieve()
                    .bodyToMono(PerformanceAggregationResponseDto.class)
                    .block();
            
            if (response != null && response.getVariantMappings() == null) {
                response.setVariantMappings(Collections.emptyMap());
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch analytics", e);
            return PerformanceAggregationResponseDto.builder()
                    .topProducts(Collections.emptyList())
                    .topBrands(Collections.emptyList())
                    .topCategories(Collections.emptyList())
                    .variantMappings(Collections.emptyMap())
                    .build();
        }
    }
}
