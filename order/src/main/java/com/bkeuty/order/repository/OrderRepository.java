package com.bkeuty.order.repository;

import com.bkeuty.order.dto.admin.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.enums.PaymentStatus;
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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate)")
    Long countOrdersByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate)")
    BigDecimal sumRevenueByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT SUM(i.quantity) FROM OrderItem i JOIN i.order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate)")
    Long sumProductsSoldByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT new com.bkeuty.order.dto.admin.VariantPerformanceDto(i.productVariantId, SUM(CAST(i.quantity AS long)), SUM(i.price * i.quantity)) FROM OrderItem i JOIN i.order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate) GROUP BY i.productVariantId")
    List<VariantPerformanceDto> findVariantPerformanceByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT new com.bkeuty.order.dto.admin.ChartDataDto(CAST(o.orderDate AS date), SUM(o.total), COUNT(o)) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate) GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date) ASC")
    List<ChartDataDto> findRevenueChartDataByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT new com.bkeuty.order.dto.admin.TopCustomerDto(o.userId, MIN(o.userName), COUNT(o), SUM(o.total)) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate) GROUP BY o.userId ORDER BY SUM(o.total) DESC")
    List<TopCustomerDto> findTopCustomers(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses,
        Pageable pageable);

    @Query("SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(cast(o.id as string), o.userName, o.orderDate, o.total, o.status) FROM Order o ORDER BY o.id DESC")
    List<DashboardOrderDto> findRecentOrders(Pageable pageable);

    @Query("SELECT new com.bkeuty.order.dto.admin.DashboardOrderDto(cast(o.id as string), o.userName, o.orderDate, o.total, o.status) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate) ORDER BY o.orderDate DESC")
    List<DashboardOrderDto> findAllOrdersInDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);

    @Query("SELECT new com.bkeuty.order.dto.admin.DailyProductPerformanceDto(o.orderDate, i.productVariantId, i.productVariantName, CAST(i.quantity AS long), i.price * i.quantity) FROM OrderItem i JOIN i.order o WHERE o.status IN :statuses AND o.orderDate >= COALESCE(:startDate, o.orderDate) AND o.orderDate <= COALESCE(:endDate, o.orderDate) ORDER BY o.orderDate DESC")
    List<DailyProductPerformanceDto> findDetailedItemPerformance(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("statuses") Collection<PaymentStatus> statuses);
}
