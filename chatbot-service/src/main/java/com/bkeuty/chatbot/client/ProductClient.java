package com.bkeuty.chatbot.client;

import com.bkeuty.chatbot.dto.ProductDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {
    private final WebClient.Builder webClientBuilder;

    public String getProductContext() {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://product/api/product?size=50&status=ACTIVE&minStock=1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching product context: {}", e.getMessage());
            return "[]";
        }
    }

    public ProductDetailDto getProductById(Integer id) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://product/api/product/{productId}", id)
                    .retrieve()
                    .bodyToMono(ProductDetailDto.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching product detail data for ID {}: {}", id, e.getMessage());
            return null;
        }
    }
}
