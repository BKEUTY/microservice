package com.bkeuty.review_service.microservicecommunication;

import com.bkeuty.review_service.dto.internal.CheckOrderDeliveredRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OrderService {
    private final WebClient orderWebClient;

    public OrderService(WebClient orderWebClient) {
        this.orderWebClient = orderWebClient;
    }

    public Boolean checkOrderIsDelivered(String userId, Integer variantId) {
        CheckOrderDeliveredRequestDto requestDto = CheckOrderDeliveredRequestDto.builder()
                .userId(userId)
                .variantId(variantId)
                .build();

        return orderWebClient.post()
                .uri("/api/order/internal/check-delivered")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
