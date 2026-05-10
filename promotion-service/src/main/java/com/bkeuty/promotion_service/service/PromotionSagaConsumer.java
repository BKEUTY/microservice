package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionSagaConsumer {

    private final PromotionService promotionService;

    @KafkaListener(topics = "order-failed-topic", groupId = "promotion-service-group")
    public void handleOrderFailed(OrderEventDto event) {
        if (event.getVoucherId() != null) {
            promotionService.refundVoucher(event.getUserId(), event.getVoucherId());
        }
    }

    @KafkaListener(topics = "order-completed-topic", groupId = "promotion-service-group")
    public void handleOrderCompleted(OrderEventDto event) {
        if (event.getVoucherId() != null) {
            promotionService.commitVoucherUsage(event.getUserId(), event.getVoucherId());
        }
    }
}
