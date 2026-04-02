package com.bkeuty.order.controller.order;

import com.bkeuty.order.dto.internal.CheckOrderDeliveredRequestDto;
import com.bkeuty.order.enums.PaymentStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
            
        boolean hasCompletedOrder = orderItemRepository.existsByOrder_UserIdAndProductVariantIdAndOrder_Status(
                request.getUserId(), request.getVariantId(), PaymentStatus.COMPLETED);
                
        return ResponseEntity.ok(hasCompletedOrder);
    }
}
