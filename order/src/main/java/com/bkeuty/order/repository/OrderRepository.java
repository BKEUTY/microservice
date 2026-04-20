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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Long countOrdersByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0)) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    BigDecimal sumRevenueByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT SUM(i.quantity) FROM OrderItem i JOIN i.order o WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Long sumProductsSoldByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.VariantPerformanceDto(
            i.productVariantId,
            SUM(CAST(i.quantity AS long)),
            SUM(CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity)
        ) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        GROUP BY i.productVariantId""")
    List<VariantPerformanceDto> findVariantPerformanceByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.ChartDataDto(
            CAST(o.orderDate AS date), SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0)), COUNT(o)
        ) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        GROUP BY CAST(o.orderDate AS date)
        ORDER BY CAST(o.orderDate AS date) ASC""")
    List<ChartDataDto> findRevenueChartDataByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.TopCustomerDto(
            o.userId, MIN(o.userName), COUNT(o), SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0))
        ) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        GROUP BY o.userId
        ORDER BY SUM(COALESCE(o.total, 0) + COALESCE(o.shippingFee, 0)) DESC""")
    List<TopCustomerDto> findTopCustomers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses,
        Pageable pageable);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(
            cast(o.id as string), o.userName, o.orderDate, COALESCE(o.total, 0), COALESCE(o.shippingFee, 0), cast(o.status as string)
        ) FROM Order o
        ORDER BY o.id DESC""")
    List<DashboardOrderDto> findRecentOrders(Pageable pageable);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(
            cast(o.id as string), o.userName, o.orderDate, COALESCE(o.total, 0), COALESCE(o.shippingFee, 0), cast(o.status as string)
        ) FROM Order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        ORDER BY o.orderDate DESC""")
    List<DashboardOrderDto> findAllOrdersInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
        SELECT new com.bkeuty.order.dto.admin.DailyProductPerformanceDto(
            o.orderDate, i.productVariantId, i.productVariantName,
            CAST(i.quantity AS long),
            CASE WHEN i.promotionPrice IS NOT NULL AND i.promotionPrice < i.productVariantPrice
                THEN i.promotionPrice ELSE i.productVariantPrice END * i.quantity
        ) FROM OrderItem i JOIN i.order o
        WHERE o.status IN :statuses AND o.orderDate >= :startDate AND o.orderDate <= :endDate
        ORDER BY o.orderDate DESC""")
    List<DailyProductPerformanceDto> findDetailedItemPerformance(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") Collection<OrderStatus> statuses);
}
