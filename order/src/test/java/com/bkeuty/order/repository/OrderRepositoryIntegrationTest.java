package com.bkeuty.order.repository;

import com.bkeuty.order.dto.admin.ChartDataDto;
import com.bkeuty.order.dto.admin.TopCustomerDto;
import com.bkeuty.order.dto.admin.VariantPerformanceDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private final LocalDateTime START = LocalDateTime.of(2026, 5, 1, 0, 0);
    private final LocalDateTime END = LocalDateTime.of(2026, 5, 31, 23, 59);
    private final List<OrderStatus> COMPLETED_STATUSES = List.of(OrderStatus.SUCCEEDED);

    @BeforeEach
    void seedData() {
        // Order 1: SUCCEEDED - User A
        Order order1 = Order.builder()
                .userId("user-A")
                .userName("Nguyen Van A")
                .total(new BigDecimal("500000"))
                .shippingFee(new BigDecimal("30000"))
                .paymentMethod(PaymentMethod.BANK)
                .orderDate(LocalDateTime.of(2026, 5, 10, 14, 0))
                .status(OrderStatus.SUCCEEDED)
                .address("123 Le Loi, Q1, HCM")
                .build();
        entityManager.persist(order1);

        OrderItem item1 = OrderItem.builder()
                .order(order1)
                .productVariantId(101)
                .productVariantName("Kem duong am A")
                .productVariantPrice(new BigDecimal("200000"))
                .quantity(2)
                .voucherDiscountAmount(BigDecimal.ZERO)
                .build();
        entityManager.persist(item1);

        OrderItem item2 = OrderItem.builder()
                .order(order1)
                .productVariantId(102)
                .productVariantName("Son moi B")
                .productVariantPrice(new BigDecimal("150000"))
                .promotionPrice(new BigDecimal("100000"))
                .quantity(1)
                .voucherDiscountAmount(BigDecimal.ZERO)
                .build();
        entityManager.persist(item2);

        // Order 2: SUCCEEDED - User B
        Order order2 = Order.builder()
                .userId("user-B")
                .userName("Tran Thi B")
                .total(new BigDecimal("800000"))
                .shippingFee(new BigDecimal("25000"))
                .paymentMethod(PaymentMethod.COD)
                .orderDate(LocalDateTime.of(2026, 5, 15, 10, 0))
                .status(OrderStatus.SUCCEEDED)
                .address("456 Nguyen Hue, Q1, HCM")
                .build();
        entityManager.persist(order2);

        OrderItem item3 = OrderItem.builder()
                .order(order2)
                .productVariantId(101)
                .productVariantName("Kem duong am A")
                .productVariantPrice(new BigDecimal("200000"))
                .quantity(4)
                .voucherDiscountAmount(new BigDecimal("20000"))
                .build();
        entityManager.persist(item3);

        // Order 3: CANCELLED - User A (should be excluded from reports)
        Order order3 = Order.builder()
                .userId("user-A")
                .userName("Nguyen Van A")
                .total(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("15000"))
                .paymentMethod(PaymentMethod.BANK)
                .orderDate(LocalDateTime.of(2026, 5, 12, 8, 0))
                .status(OrderStatus.CANCELLED)
                .address("789 CMT8, Q3, HCM")
                .build();
        entityManager.persist(order3);

        entityManager.flush();
    }

    // === DATA LAYER: Dashboard Aggregate Queries ===

    @Test
    void countOrdersByDateRangeAndStatus_ShouldReturnCorrectCount() {
        Long count = orderRepository.countOrdersByDateRangeAndStatus(START, END, COMPLETED_STATUSES);
        assertEquals(2L, count, "Should count only SUCCEEDED orders");
    }

    @Test
    void sumRevenueByDateRangeAndStatus_ShouldSumOnlySucceededOrders() {
        BigDecimal revenue = orderRepository.sumRevenueByDateRangeAndStatus(START, END, COMPLETED_STATUSES);
        assertEquals(0, new BigDecimal("1300000").compareTo(revenue),
                "Revenue should be 500000 + 800000 = 1300000");
    }

    @Test
    void sumShippingFeeByDateRangeAndStatus_ShouldSumCorrectly() {
        BigDecimal shippingFee = orderRepository.sumShippingFeeByDateRangeAndStatus(START, END, COMPLETED_STATUSES);
        assertEquals(0, new BigDecimal("55000").compareTo(shippingFee),
                "Shipping fees should be 30000 + 25000 = 55000");
    }

    @Test
    void sumProductsSoldByDateRangeAndStatus_ShouldSumQuantities() {
        Long productsSold = orderRepository.sumProductsSoldByDateRangeAndStatus(START, END, COMPLETED_STATUSES);
        assertEquals(7L, productsSold, "Total items sold: 2 + 1 + 4 = 7");
    }

    @Test
    void findVariantPerformanceByDateRangeAndStatus_ShouldAggregateByVariant() {
        List<VariantPerformanceDto> results = orderRepository
                .findVariantPerformanceByDateRangeAndStatus(START, END, COMPLETED_STATUSES);

        assertNotNull(results);
        assertEquals(2, results.size(), "Should have 2 distinct variants");

        // Variant 101: qty=2+4=6
        VariantPerformanceDto variant101 = results.stream()
                .filter(v -> v.getVariantId() == 101)
                .findFirst().orElseThrow();
        assertEquals(6L, variant101.getQuantity());
    }

    @Test
    void findRevenueChartDataByDateRange_ShouldGroupByDate() {
        List<ChartDataDto> chartData = orderRepository
                .findRevenueChartDataByDateRange(START, END, COMPLETED_STATUSES);

        assertNotNull(chartData);
        assertEquals(2, chartData.size(), "Should have 2 distinct days (May 10, May 15)");
    }

    @Test
    void findTopCustomers_ShouldRankByTotalSpending() {
        List<TopCustomerDto> topCustomers = orderRepository
                .findTopCustomers(START, END, COMPLETED_STATUSES, PageRequest.of(0, 10));

        assertNotNull(topCustomers);
        assertEquals(2, topCustomers.size());
        assertEquals("user-B", topCustomers.get(0).getUserId(),
                "User B should be top customer with 825000 total spending");
    }

    // === EDGE CASE: Empty Date Range ===

    @Test
    void countOrdersByDateRangeAndStatus_ShouldReturnZero_WhenNoOrdersInRange() {
        LocalDateTime futureStart = LocalDateTime.of(2099, 1, 1, 0, 0);
        LocalDateTime futureEnd = LocalDateTime.of(2099, 12, 31, 23, 59);

        Long count = orderRepository.countOrdersByDateRangeAndStatus(futureStart, futureEnd, COMPLETED_STATUSES);
        assertNull(count == null ? null : (count == 0 ? null : count),
                "Should return 0 or null for empty date range");
    }

    @Test
    void sumTotalSpendingByUserId_ShouldCalculateOnlySucceededOrders() {
        BigDecimal spending = orderRepository.sumTotalSpendingByUserId("user-A", COMPLETED_STATUSES);
        assertEquals(0, new BigDecimal("530000").compareTo(spending),
                "User A total spending: 500000 + 30000 = 530000 (only SUCCEEDED order)");
    }

    @Test
    void findByShippingCode_ShouldReturnNull_WhenCodeNotFound() {
        Order order = orderRepository.findByShippingCode("NONEXISTENT-CODE");
        assertNull(order);
    }
}
