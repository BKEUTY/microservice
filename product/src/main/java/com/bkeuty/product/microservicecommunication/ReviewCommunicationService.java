package com.bkeuty.product.microservicecommunication;

import com.bkeuty.product.dto.user.product.ReviewPreviewDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewCommunicationService {
    private final WebClient reviewWebClient;

    public ReviewCommunicationService(WebClient reviewWebClient) {
        this.reviewWebClient = reviewWebClient;
    }

    public List<ReviewPreviewDto> getLatestReviews(Integer variantId) {
        try {
            Map<String, Object> response = reviewWebClient.get()
                    .uri("/api/reviews/product/" + variantId + "?page=0&size=5")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                return content.stream().map(this::mapToReviewPreview).toList();
            }
        } catch (Exception e) {
            log.error("Error fetching latest reviews from review-service for variantId: {}", variantId, e);
        }
        return Collections.emptyList();
    }

    private ReviewPreviewDto mapToReviewPreview(Map<String, Object> map) {
        String createdAtStr = (String) map.get("createdAt");
        LocalDateTime createdAt = null;
        if (createdAtStr != null) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr);
            } catch (Exception e) {
                log.warn("Failed to parse createdAt date: {}", createdAtStr);
            }
        }

        return ReviewPreviewDto.builder()
                .id(((Number) map.get("id")).longValue())
                .userName((String) map.get("userName"))
                .rating((Integer) map.get("rating"))
                .comment((String) map.get("comment"))
                .images((List<String>) map.get("images"))
                .createdAt(createdAt)
                .build();
    }
}
