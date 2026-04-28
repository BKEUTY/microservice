package com.bkeuty.product.service.recommendation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bkeuty.product.dto.recommendation.RecommendationResponse;
import com.bkeuty.product.dto.user.product.CategoryDto;
import com.bkeuty.product.dto.user.product.DisplayProductDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.microservicecommunication.OrderServiceCommunication;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.microservicecommunication.ReviewServiceCommunication;
import com.bkeuty.product.repository.ProductVariantRepository;

@Service
public class RecommendationService {
    private final ProductVariantRepository productVariantRepository;
    private final OrderServiceCommunication orderServiceCommunication;
    private final PromotionService promotionService;
    private final AIRankingService aiRankingService;
    private final ReviewServiceCommunication reviewServiceCommunication;

    public RecommendationService(ProductVariantRepository productVariantRepository,
                                OrderServiceCommunication orderServiceCommunication,
                                PromotionService promotionService,
                                AIRankingService aiRankingService,
                                ReviewServiceCommunication reviewServiceCommunication) {
        this.productVariantRepository = productVariantRepository;
        this.orderServiceCommunication = orderServiceCommunication;
        this.promotionService = promotionService;
        this.aiRankingService = aiRankingService;
        this.reviewServiceCommunication = reviewServiceCommunication;
    }

    @Cacheable(value = "recommendations", key = "'guest'", condition = "#userId == null")
    public RecommendationResponse getPersonalizedRecommendations(String userId) {
        List<ProductVariant> candidates = productVariantRepository.findActiveVariantsWithStock(PageRequest.of(0, 30));

        if (userId == null) {
            AIRankingService.AIResult guestResult = aiRankingService.getVariedRecommendations(candidates, "personalized:guest");
            return buildResponse(guestResult);
        }

        List<Map<String, Object>> history = orderServiceCommunication.getOrderHistory(userId);
        List<Map<String, Object>> cart = orderServiceCommunication.getCartItems(userId);
        List<Map<String, Object>> reviews = reviewServiceCommunication.getUserReviews(userId);
        
        if (history.isEmpty() && cart.isEmpty() && reviews.isEmpty()) {
            AIRankingService.AIResult emptyHistoryResult = aiRankingService.getVariedRecommendations(candidates, "personalized:" + userId + ":new_user");
            return buildResponse(emptyHistoryResult);
        }

        AIRankingService.AIResult aiResult = aiRankingService.getRankedAIResult("Personalized Feed", history, cart, reviews, candidates, "personalized:" + userId);
        return buildResponse(aiResult);
    }

    public RecommendationResponse getRelatedProducts(String productName) {
        List<ProductVariant> candidates = productVariantRepository.findActiveVariantsWithStock(PageRequest.of(0, 30))
                .stream()
                .filter(v -> !v.getProductVariantName().equalsIgnoreCase(productName))
                .collect(Collectors.toList());
        
        String context = String.format("Product Context: Currently viewing '%s'. Suggest related or complementary items.", productName);
        
        AIRankingService.AIResult aiResult = aiRankingService.getRankedAIResult(context, List.of(), List.of(), List.of(), candidates, "related:" + productName);
        return buildResponse(aiResult);
    }

    private RecommendationResponse buildResponse(AIRankingService.AIResult aiResult) {
        return RecommendationResponse.builder()
                .recommendation(aiResult.getReasoning())
                .recommendedProducts(fetchAndMapToDtos(aiResult.getProductIds()))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private List<DisplayProductDto> fetchAndMapToDtos(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
        if (variants.isEmpty()) return Collections.emptyList();

        Map<Integer, PromotionPriceDto> promoMap = promotionService.getListOfPromotionPrice(variants);
        Map<Integer, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        return ids.stream()
                .map(variantMap::get)
                .filter(Objects::nonNull)
                .map(v -> {
                    if (v.getStockQuantity() == null || v.getStockQuantity() <= 0) {
                        return null;
                    }
                    PromotionPriceDto promo = promoMap.get(v.getId());
                    return DisplayProductDto.builder()
                            .productId(v.getId())
                            .variantName(v.getProductVariantName())
                            .description(v.getDescription())
                            .imageUrl(v.getProductImageUrl())
                            .originPrice(v.getPrice())
                            .discountPrice(promo != null ? promo.getNewPrice() : v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .sold(v.getSold())
                            .brand(v.getProduct().getBrand() != null ? v.getProduct().getBrand().getBrandName() : "N/A")
                            .categories(v.getProduct().getCategories().stream()
                                    .map(cat -> new CategoryDto(cat.getId(), cat.getCategoryName()))
                                    .collect(Collectors.toList()))
                            .status(v.getStatus() != null ? v.getStatus().name() : "UNKNOWN")
                            .averageRating(v.getAverageRating())
                            .reviewCount(v.getReviewCount())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"recommendations"}, allEntries = true)
    public void evictAllAIRecommendationCache() {}
}
