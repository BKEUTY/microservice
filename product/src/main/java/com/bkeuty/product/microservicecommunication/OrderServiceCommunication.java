package com.bkeuty.product.microservicecommunication;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceCommunication {
    private final WebClient orderWebClient;

    public OrderServiceCommunication(@Qualifier("orderWebClient") WebClient orderWebClient) {
        this.orderWebClient = orderWebClient;
    }

    public List<Map<String, Object>> getOrderHistory(String userId) {
        try {
            return orderWebClient.get()
                    .uri("/api/order/internal/history/" + userId)
                    .retrieve()
                    .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getCartItems(String userId) {
        try {
            return orderWebClient.get()
                    .uri("/api/order/internal/cart/" + userId)
                    .retrieve()
                    .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }
}
