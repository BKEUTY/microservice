package com.bkeuty.order.repository;

import com.bkeuty.order.dto.admin.ChartDataDto;
import com.bkeuty.order.dto.admin.DailyProductPerformanceDto;
import com.bkeuty.order.dto.admin.DashboardOrderDto;
import com.bkeuty.order.dto.admin.TopCustomerDto;
import com.bkeuty.order.dto.admin.VariantPerformanceDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    List<Order> findByUserId(String userId);

    Order findByIdAndUserId(Integer orderId,String userId);
    Order findByShippingCode(String shippingCode);
    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND EXISTS (SELECT 1 FROM OrderItem i WHERE i.order = o AND i.refundOrder IS NULL)""")
    Long countOrdersByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT SUM(
            (CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity)
            - COALESCE(i.voucherDiscountAmount, 0.0)
        ) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND i.refundOrder IS NULL""")
    BigDecimal sumRevenueByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT SUM(COALESCE(o.shippingFee, 0)) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND EXISTS (SELECT 1 FROM OrderItem i WHERE i.order = o AND i.refundOrder IS NULL)""")
    BigDecimal sumShippingFeeByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT SUM(i.quantity) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND i.refundOrder IS NULL""")
    Long sumProductsSoldByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.VariantPerformanceDto(
            i.productVariantId,
            SUM(CAST(i.quantity AS long)),
            SUM((CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(i.voucherDiscountAmount, 0.0))
        ) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND i.refundOrder IS NULL
        GROUP BY i.productVariantId""")
    List<VariantPerformanceDto> findVariantPerformanceByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.ChartDataDto(
            CAST(o.orderDate AS date),
            SUM((CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(i.voucherDiscountAmount, 0.0)),
            CAST(0.0 AS big_decimal),
            COUNT(DISTINCT o.id)
        ) FROM Order o JOIN o.orderItems i
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND i.refundOrder IS NULL
        GROUP BY CAST(o.orderDate AS date)
        ORDER BY CAST(o.orderDate AS date) ASC""")
    List<ChartDataDto> findRevenueChartDataByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT CAST(o.orderDate AS date) as date,
               SUM(COALESCE(o.shippingFee, 0)) as shippingFee
        FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
          AND EXISTS (SELECT 1 FROM OrderItem i WHERE i.order = o AND i.refundOrder IS NULL)
        GROUP BY CAST(o.orderDate AS date)""")
    List<Object[]> findShippingFeeChartDataByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.TopCustomerDto(
            o.userId, 
            MIN(o.userName), 
            COUNT(DISTINCT CASE WHEN EXISTS (SELECT 1 FROM OrderItem i2 WHERE i2.order = o AND i2.refundOrder IS NULL) THEN o.id ELSE NULL END), 
            SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0)) - 
            COALESCE(
                (SELECT SUM(
                    (CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                        THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity)
                    - COALESCE(i.voucherDiscountAmount, 0.0)
                 ) FROM OrderItem i JOIN i.order o2 
                 WHERE o2.userId = o.userId AND o2.status IN :statuses AND o2.orderDate >= :startDate AND o2.orderDate <= :endDate
                   AND i.refundOrder IS NOT NULL), 
                0.0
            )
        ) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        GROUP BY o.userId
        ORDER BY (SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0)) - 
            COALESCE(
                (SELECT SUM(
                    (CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                        THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity)
                    - COALESCE(i.voucherDiscountAmount, 0.0)
                 ) FROM OrderItem i JOIN i.order o2 
                 WHERE o2.userId = o.userId AND o2.status IN :statuses AND o2.orderDate >= :startDate AND o2.orderDate <= :endDate
                   AND i.refundOrder IS NOT NULL), 
                0.0
            )) DESC""")
    List<TopCustomerDto> findTopCustomers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses,
        Pageable pageable);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(
            cast(o.id as string), o.userName, o.orderDate, 
            COALESCE(SUM(i.productVariantPrice * i.quantity), 0),
            COALESCE(SUM(CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity), 0),
            COALESCE(o.voucherDiscountAmount, 0),
            COALESCE(o.shippingFee, 0),
            CASE WHEN COUNT(i.id) > 0 
                 THEN (SUM(CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(o.voucherDiscountAmount, 0)) 
                 ELSE 0 END,
            COALESCE(SUM(CASE WHEN i.refundOrder IS NOT NULL THEN ((CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(i.voucherDiscountAmount, 0)) ELSE 0 END), 0),
            cast(o.status as string)
        ) FROM Order o LEFT JOIN o.orderItems i
        GROUP BY o.id, o.userName, o.orderDate, o.total, o.shippingFee, o.status, o.voucherDiscountAmount
        ORDER BY o.id DESC""")
    List<DashboardOrderDto> findRecentOrders(Pageable pageable);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(
            cast(o.id as string), o.userName, o.orderDate,
            COALESCE(SUM(i.productVariantPrice * i.quantity), 0),
            COALESCE(SUM(CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity), 0),
            COALESCE(o.voucherDiscountAmount, 0),
            COALESCE(o.shippingFee, 0),
            CASE WHEN COUNT(i.id) > 0 
                 THEN (SUM(CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(o.voucherDiscountAmount, 0)) 
                 ELSE 0 END,
            COALESCE(SUM(CASE WHEN i.refundOrder IS NOT NULL THEN ((CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(i.voucherDiscountAmount, 0)) ELSE 0 END), 0),
            cast(o.status as string)
        ) FROM Order o LEFT JOIN o.orderItems i
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        GROUP BY o.id, o.userName, o.orderDate, o.total, o.shippingFee, o.status, o.voucherDiscountAmount
        ORDER BY o.orderDate DESC""")
    List<DashboardOrderDto> findAllOrdersInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.total + o.shippingFee), 0) FROM Order o WHERE o.userId = :userId AND o.status IN :statuses")
    BigDecimal sumTotalSpendingByUserId(@Param("userId") String userId, @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DailyProductPerformanceDto(
            o.orderDate, i.productVariantId, i.productVariantName, CAST(i.quantity AS long),
            (CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity) - COALESCE(i.voucherDiscountAmount, 0.0),
            i.productVariantPrice, i.promotionPrice, COALESCE(i.voucherDiscountAmount, 0.0),
            CASE WHEN i.refundOrder IS NOT NULL THEN true ELSE false END
        ) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        ORDER BY o.orderDate DESC""")
    List<DailyProductPerformanceDto> findDetailedItemPerformance(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);
}
