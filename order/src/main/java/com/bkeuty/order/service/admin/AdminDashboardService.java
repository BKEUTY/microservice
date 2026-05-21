package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.*;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Transactional(readOnly = true)
@Slf4j
public class AdminDashboardService {

    private static final List<OrderStatus> COMPLETED_STATUSES = List.of(OrderStatus.SUCCEEDED);
    private static final LocalDateTime DEFAULT_START = LocalDateTime.of(2000, 1, 1, 0, 0);

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
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : DEFAULT_START;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        Long totalOrders = orderRepository.countOrdersByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        BigDecimal totalRevenue = orderRepository.sumRevenueByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        BigDecimal totalShippingFee = orderRepository.sumShippingFeeByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        Long totalProductsSold = orderRepository.sumProductsSoldByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        Long totalUsers = fetchUserCount(start, end, token);

        Double ordersGrowth = 0.0;
        Double revenueGrowth = 0.0;
        Double productsSoldGrowth = 0.0;
        Double customersGrowth = 0.0;

        if (startDate != null) {
            long days = ChronoUnit.DAYS.between(start, end);

            LocalDateTime prevStart = start.minusDays(days + 1);
            LocalDateTime prevEnd = start.minusNanos(1);

            Long prevOrders = orderRepository.countOrdersByDateRangeAndStatus(prevStart, prevEnd, COMPLETED_STATUSES);
            BigDecimal prevRevenue = orderRepository.sumRevenueByDateRangeAndStatus(prevStart, prevEnd, COMPLETED_STATUSES);
            Long prevProductsSold = orderRepository.sumProductsSoldByDateRangeAndStatus(prevStart, prevEnd, COMPLETED_STATUSES);
            Long prevUsers = fetchUserCount(prevStart, prevEnd, token);

            ordersGrowth = calculateGrowth(prevOrders, totalOrders);
            revenueGrowth = calculateGrowth(prevRevenue, totalRevenue);
            productsSoldGrowth = calculateGrowth(prevProductsSold, totalProductsSold);
            customersGrowth = calculateGrowth(prevUsers, totalUsers);
        }

        List<VariantPerformanceDto> variantPerformances = orderRepository.findVariantPerformanceByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        PerformanceAggregationResponseDto analytics = fetchTopPerformers(variantPerformances, token);

        BigDecimal totalProfit = totalRevenue != null ? totalRevenue.multiply(BigDecimal.valueOf(0.40)) : BigDecimal.ZERO;

        DashboardDto.Overview overview = DashboardDto.Overview.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .ordersGrowth(ordersGrowth)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .revenueGrowth(revenueGrowth)
                .totalShippingFee(totalShippingFee != null ? totalShippingFee : BigDecimal.ZERO)
                .totalProfit(totalProfit)
                .totalProductsSold(totalProductsSold != null ? totalProductsSold : 0L)
                .productsSoldGrowth(productsSoldGrowth)
                .totalRegisteredCustomers(totalUsers != null ? totalUsers : 0L)
                .customersGrowth(customersGrowth)
                .build();


        List<ChartDataDto> chartData = orderRepository.findRevenueChartDataByDateRange(start, end, COMPLETED_STATUSES);
        List<Object[]> shippingFeeData = orderRepository.findShippingFeeChartDataByDateRange(start, end, COMPLETED_STATUSES);

        Map<LocalDate, BigDecimal> shippingFeeMap = new HashMap<>();
        if (shippingFeeData != null) {
            for (Object[] row : shippingFeeData) {
                LocalDate date = convertToLocalDate(row[0]);
                BigDecimal fee = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                if (date != null) {
                    shippingFeeMap.put(date, fee);
                }
            }
        }

        if (chartData != null) {
            for (ChartDataDto dto : chartData) {
                if (dto.getDate() != null && shippingFeeMap.containsKey(dto.getDate())) {
                    dto.setShippingFee(shippingFeeMap.get(dto.getDate()));
                }
            }
        }

        List<DailyProductPerformanceDto> productDetail = orderRepository.findDetailedItemPerformance(start, end, COMPLETED_STATUSES);
        if (productDetail == null) productDetail = new ArrayList<>();

        List<TransactionalPerformanceDto> brandDetail = new ArrayList<>();
        List<TransactionalPerformanceDto> categoryDetail = new ArrayList<>();

        if (analytics != null && productDetail != null) {
            Map<Integer, VariantMappingDto> mappings = analytics.getVariantMappings() != null
                ? analytics.getVariantMappings() : Collections.emptyMap();

            productDetail.forEach(item -> {
                VariantMappingDto mapping = mappings.get(item.getVariantId());
                if (mapping == null) return;
                item.setName(mapping.getVariantName());
                if (mapping.getBrandId() != null) {
                    brandDetail.add(new TransactionalPerformanceDto(
                        item.getDate(), mapping.getBrandId(), mapping.getBrandName(),
                        item.getVariantId(), mapping.getVariantName(),
                        item.getQuantity(), item.getRevenue(), item.getOriginalPrice(), item.getPromotionalPrice(), item.getVoucherDiscount(), item.getIsRefunded()));
                }
                if (mapping.getCategoryId() != null) {
                    categoryDetail.add(new TransactionalPerformanceDto(
                        item.getDate(), mapping.getCategoryId(), mapping.getCategoryName(),
                        item.getVariantId(), mapping.getVariantName(),
                        item.getQuantity(), item.getRevenue(), item.getOriginalPrice(), item.getPromotionalPrice(), item.getVoucherDiscount(), item.getIsRefunded()));
                }
            });
        }

        List<TopCustomerDto> topCustomers = orderRepository.findTopCustomers(start, end, COMPLETED_STATUSES, PageRequest.of(0, 10));
        List<DashboardOrderDto> recentOrders = orderRepository.findAllOrdersInDateRange(start, end, COMPLETED_STATUSES);

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
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : DEFAULT_START;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        return orderRepository.findAllOrdersInDateRange(start, end, COMPLETED_STATUSES);
    }

    public PerformanceAggregationResponseDto getDetailedProducts(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : DEFAULT_START;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        List<VariantPerformanceDto> performances = orderRepository.findVariantPerformanceByDateRangeAndStatus(start, end, COMPLETED_STATUSES);
        return fetchTopPerformers(performances, token);
    }

    public List<TopCustomerDto> getDetailedCustomers(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : DEFAULT_START;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        return orderRepository.findTopCustomers(start, end, COMPLETED_STATUSES, PageRequest.of(0, 100));
    }

    public List<UserDetailDto> getDetailedNewUsers(LocalDate startDate, LocalDate endDate, String token) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : DEFAULT_START;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        try {
            long startMs = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMs = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return userWebClient.get()
                    .uri(b -> b.path("/api/user/internal/list")
                                .queryParam("startDate", startMs)
                                .queryParam("endDate", endMs)
                                .build())
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToFlux(UserDetailDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch new users", e);
            return Collections.emptyList();
        }
    }

    private Long fetchUserCount(LocalDateTime start, LocalDateTime end, String token) {
        try {
            long startMs = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMs = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return userWebClient.get()
                    .uri(b -> b.path("/api/user/internal/count")
                                .queryParam("startDate", startMs)
                                .queryParam("endDate", endMs)
                                .build())
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch user count", e);
            return 0L;
        }
    }

    private PerformanceAggregationResponseDto fetchTopPerformers(List<VariantPerformanceDto> performances, String token) {
        if (performances == null || performances.isEmpty()) {
            return emptyPerformance();
        }
        try {
            PerformanceAggregationResponseDto response = productWebClient.post()
                    .uri("/api/product/internal/analytics/aggregate")
                    .header("Authorization", token)
                    .bodyValue(performances)
                    .retrieve()
                    .bodyToMono(PerformanceAggregationResponseDto.class)
                    .block();
            if (response != null && response.getVariantMappings() == null) {
                response.setVariantMappings(Collections.emptyMap());
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch analytics", e);
            return emptyPerformance();
        }
    }

    private Double calculateGrowth(Long previous, Long current) {
        return calculateGrowth(
            previous != null ? BigDecimal.valueOf(previous) : BigDecimal.ZERO,
            current != null ? BigDecimal.valueOf(current) : BigDecimal.ZERO
        );
    }

    private Double calculateGrowth(BigDecimal previous, BigDecimal current) {
        BigDecimal prev = previous != null ? previous : BigDecimal.ZERO;
        BigDecimal curr = current != null ? current : BigDecimal.ZERO;

        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            return curr.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return curr.subtract(prev)
                .divide(prev, 4, RoundingMode.HALF_UP)

                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private PerformanceAggregationResponseDto emptyPerformance() {
        return PerformanceAggregationResponseDto.builder()
                .topProducts(Collections.emptyList())
                .topBrands(Collections.emptyList())
                .topCategories(Collections.emptyList())
                .variantMappings(Collections.emptyMap())
                .build();
    }

    private LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) return null;
        if (dateObj instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        } else if (dateObj instanceof java.time.LocalDate localDate) {
            return localDate;
        } else if (dateObj instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (dateObj instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        return null;
    }
}
