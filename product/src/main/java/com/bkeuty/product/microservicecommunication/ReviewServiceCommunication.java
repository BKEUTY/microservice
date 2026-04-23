package com.bkeuty.product.microservicecommunication;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceCommunication {
    private final WebClient reviewWebClient;

    public ReviewServiceCommunication(WebClient reviewWebClient) {
        this.reviewWebClient = reviewWebClient;
    }

    public List<Map<String, Object>> getUserReviews(String userId) {
        try {
            return reviewWebClient.get()
                    .uri("/api/review/internal/history/" + userId)
                    .retrieve()
                    .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }
}
