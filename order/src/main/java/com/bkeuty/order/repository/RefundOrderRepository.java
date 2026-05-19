package com.bkeuty.order.repository;

import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.RefundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface RefundOrderRepository extends JpaRepository<RefundOrder, Integer>,
        JpaSpecificationExecutor<RefundOrder> {
    Page<RefundOrder> findByUserId(String userId, Pageable pageable);
    List<RefundOrder> findByUserId(String userId);
    List<RefundOrder> findByOrderId(Integer orderId);

    RefundOrder findByShippingCode(String shippingCode);
}
