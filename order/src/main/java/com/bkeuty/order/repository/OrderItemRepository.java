package com.bkeuty.order.repository;

import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bkeuty.order.enums.PaymentStatus;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
    List<OrderItem> findByOrderIdIn(List<Integer> orderIds);
    OrderItem findByOrderIdAndProductVariantId(Integer orderId, Integer productVariantId);
    boolean existsByOrder_UserIdAndProductVariantIdAndOrder_StatusAndIsReviewedFalse(String userId, Integer variantId, OrderStatus status);
    List<OrderItem> findAllByOrder_UserId(String userId);
    java.util.Optional<OrderItem> findFirstByOrder_UserIdAndProductVariantIdAndOrder_StatusAndIsReviewedFalse(String userId, Integer variantId, OrderStatus status);
}
