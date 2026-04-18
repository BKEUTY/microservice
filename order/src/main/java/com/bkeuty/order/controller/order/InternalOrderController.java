package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.internal.CheckOrderDeliveredRequestDto;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.bkeuty.order.entity.OrderItem;

@RestController
@RequestMapping("/api/order/internal")
public class InternalOrderController {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public InternalOrderController(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @PostMapping("/check-delivered")
    public ResponseEntity<Boolean> checkOrderDelivered(@RequestBody CheckOrderDeliveredRequestDto request) {
        boolean canReview = orderItemRepository.existsByOrder_UserIdAndProductVariantIdAndOrder_StatusAndIsReviewedFalse(
                request.getUserId(), request.getVariantId(), PaymentStatus.COMPLETED);
        return ResponseEntity.ok(canReview);
    }

    @PostMapping("/mark-reviewed")
    public ResponseEntity<Void> markReviewed(@RequestBody CheckOrderDeliveredRequestDto request) {
        OrderItem orderItem = orderItemRepository.findFirstByOrder_UserIdAndProductVariantIdAndOrder_StatusAndIsReviewedFalse(
                request.getUserId(), request.getVariantId(), PaymentStatus.COMPLETED)
                .orElse(null);
        if (orderItem != null) {
            orderItem.setReviewed(true);
            orderItemRepository.save(orderItem);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<java.util.List<OrderItem>> getOrderHistory(@PathVariable String userId) {
        return ResponseEntity.ok(orderItemRepository.findAllByOrder_UserId(userId));
    }
}
