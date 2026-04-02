package com.bkeuty.order.repository;

import com.bkeuty.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bkeuty.order.enums.PaymentStatus;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
    List<OrderItem> findByOrderIdIn(List<Integer> orderIds);

    boolean existsByOrder_UserIdAndProductVariantIdAndOrder_Status(String userId, Integer variantId, PaymentStatus status);
}
