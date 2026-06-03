package com.bkeuty.chatbot.client;

import com.bkeuty.chatbot.dto.ProductDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {
    @Qualifier("externalRestTemplate")
    private final RestTemplate externalRestTemplate;

    public String getProductContext(String userId, Integer membershipLevel) {
        try {
            String url = "https://backend.bkeuty.xyz/api/product?size=100&sort=sold,desc&status=ACTIVE&minStock=1";
            if (userId != null) {
                url += "&userId=" + userId;
                if (membershipLevel != null) {
                    url += "&membershipLevel=" + membershipLevel;
                }
            }
            return externalRestTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Error fetching product context: {}", e.getMessage());
            return "[]";
        }
    }

    public ProductDetailDto getProductById(Integer id, String userId, Integer membershipLevel) {
        try {
            String url = "https://backend.bkeuty.xyz/api/product/" + id;
            if (userId != null) {
                url += "?userId=" + userId;
                if (membershipLevel != null) {
                    url += "&membershipLevel=" + membershipLevel;
                }
            }
            return externalRestTemplate.getForObject(url, ProductDetailDto.class);
        } catch (Exception e) {
            log.error("Error fetching product detail data for ID {}: {}", id, e.getMessage());
            return null;
        }
    }
}
