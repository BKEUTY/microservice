package com.bkeuty.review_service.microservicecommunication;

import com.bkeuty.review_service.dto.internal.UpdateRatingRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductService {
    private final WebClient productWebClient;

    public ProductService(WebClient productWebClient) {
        this.productWebClient = productWebClient;
    }

    public void updateProductRating(Integer variantId, Double averageRating, Integer reviewCount) {
        UpdateRatingRequestDto requestDto = UpdateRatingRequestDto.builder()
                .variantId(variantId)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();

        productWebClient.post()
                .uri("/api/product/internal/update-rating")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
