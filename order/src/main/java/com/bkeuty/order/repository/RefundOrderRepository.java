package com.bkeuty.order.repository;

import com.bkeuty.order.entity.RefundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundOrderRepository extends JpaRepository<RefundOrder, Integer>,
        JpaSpecificationExecutor<RefundOrder> {
    List<RefundOrder> findByUserId(String userId);
    List<RefundOrder> findByOrderId(Integer orderId);
}
