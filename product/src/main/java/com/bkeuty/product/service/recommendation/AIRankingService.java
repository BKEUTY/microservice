package com.bkeuty.product.service.recommendation;

import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.service.ai.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRankingService {
    private static final Logger logger = LoggerFactory.getLogger(AIRankingService.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public AIRankingService(GeminiService geminiService) {
        this.geminiService = geminiService;
        this.objectMapper = new ObjectMapper();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AIResult implements Serializable {
        private String reasoning;
        private List<Integer> productIds;
    }

    @Cacheable(value = "recommendations", key = "#a5", unless = "#result == null")
    public AIResult getRankedAIResult(String profile, List<Map<String, Object>> history, List<Map<String, Object>> cart, List<Map<String, Object>> reviews, List<ProductVariant> candidates, String cacheKey) {
        try {
            String candidateJson = candidates.stream()
                    .filter(p -> p != null && p.getProduct() != null)
                    .map(p -> String.format("ID: %d, Name: %s, Category: %s, Brand: %s", 
                        p.getId(), p.getProductVariantName(), 
                        p.getProduct().getCategories() != null ? p.getProduct().getCategories().stream().map(ProductCategory::getCategoryName).collect(Collectors.joining(", ")) : "N/A",
                        p.getProduct().getBrand() != null ? p.getProduct().getBrand().getBrandName() : "N/A"))
                    .collect(Collectors.joining("\n"));

            String orderHistory = history.stream()
                    .map(item -> String.format("VariantID: %s, Qty: %s", item.get("productVariantId"), item.get("quantity")))
                    .collect(Collectors.joining("\n"));

            String cartData = cart.stream()
                    .map(item -> String.format("VariantID: %s, Qty: %s", item.get("productVariant"), item.get("quantity")))
                    .collect(Collectors.joining("\n"));

            String reviewData = reviews.stream()
                    .map(item -> String.format("VariantID: %s, Rating: %s", item.get("variantId"), item.get("rating")))
                    .collect(Collectors.joining("\n"));

            String geminiResponse = geminiService.getRankedRecommendations(profile, orderHistory, cartData, reviewData, candidateJson);
            String cleanJson = cleanGeminiJson(geminiResponse);
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            
            List<Integer> selectedIds = new ArrayList<>();
            JsonNode idNode = rootNode.path("productIds");
            if (idNode.isArray()) {
                idNode.forEach(node -> selectedIds.add(node.asInt()));
            }

            return new AIResult(rootNode.path("reasoning").asText(), selectedIds);
        } catch (Exception e) {
            logger.error("[AI-RANK] Error occurred during ranking for key {}: {}", cacheKey, e.getMessage());
            return getStaticFallback(candidates);
        }
    }

    public AIResult getVariedRecommendations(List<ProductVariant> candidates, String cacheKey) {
        List<ProductVariant> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        List<ProductVariant> pool = shuffled.stream().limit(5).collect(Collectors.toList());
        List<Integer> selectedIds = pool.stream().map(ProductVariant::getId).collect(Collectors.toList());
        
        String reasoning = cacheKey.contains("guest") 
                ? "Khám phá các sản phẩm xu hướng được yêu thích nhất tại Bkeuty!" 
                : "Chào mừng bạn! Hãy bắt đầu hành trình làm đẹp với những gợi ý nổi bật này.";
                
        return new AIResult(reasoning, selectedIds);
    }

    private AIResult getStaticFallback(List<ProductVariant> candidates) {
        List<Integer> fallbackIds = candidates.stream().limit(5).map(ProductVariant::getId).collect(Collectors.toList());
        return new AIResult("Khám phá các sản phẩm hàng đầu của chúng tôi!", fallbackIds);
    }

    private String cleanGeminiJson(String raw) {
        if (raw == null) return "{}";
        String clean = raw.trim();
        if (clean.startsWith("```json")) clean = clean.substring(7);
        if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
        return clean.trim();
    }
}
