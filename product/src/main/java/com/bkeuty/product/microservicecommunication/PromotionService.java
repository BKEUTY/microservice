package com.bkeuty.product.microservicecommunication;

import com.bkeuty.product.dto.user.product.ProductPromotionDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromotionService {
    private final WebClient promotionWebClient;
    public PromotionService(@Qualifier("promotionWebClient") WebClient promotionWebClient) {
        this.promotionWebClient = promotionWebClient;
    }
    public PromotionPriceDto getPromotionPrice(ProductVariant productVariant, String userId, Integer membershipLevel){
        try {
            PromotionPriceDto promotionPriceDto = promotionWebClient.post()
                    .uri("/api/promotion/internal/check-promotion-price")
                    .bodyValue(toProductPromotionDto(productVariant, userId, membershipLevel)).retrieve().bodyToMono(PromotionPriceDto.class).block();
            return  promotionPriceDto;
        } catch (Exception e) {
            log.error("Failed to fetch promotion price from promotion-service for variant {}: {}", productVariant.getId(), e.getMessage());
            PromotionPriceDto fallback = new PromotionPriceDto(productVariant.getPrice());
            fallback.setAppliedPromotionType("SERVICE_DOWN");
            return fallback;
        }
    }

    public PromotionPriceDto getPromotionPrice(ProductVariant productVariant){
        return getPromotionPrice(productVariant, null, 0);
    }

    public Map<Integer,PromotionPriceDto> getListOfPromotionPrice(Page<ProductVariant> productVariants, String userId, Integer membershipLevel){
        List<ProductVariant> variants = productVariants.getContent();
        List<ProductPromotionDto> productPromotionDtos = variants.stream().map(pv -> toProductPromotionDto(pv, userId, membershipLevel)).collect(Collectors.toList());
        try {
            Map<Integer,PromotionPriceDto> promotionPrice = promotionWebClient.post()
                    .uri("/api/promotion/internal/check-promotion-price/batch")
                    .bodyValue(productPromotionDtos).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer,PromotionPriceDto>>() {
                    }).block();
            return promotionPrice;
        } catch (Exception e) {
            log.error("Failed to fetch promotion prices batch from promotion-service: {}", e.getMessage());
            return variants.stream().collect(Collectors.toMap(
                    ProductVariant::getId,
                    pv -> {
                        PromotionPriceDto fallback = new PromotionPriceDto(pv.getPrice());
                        fallback.setAppliedPromotionType("SERVICE_DOWN");
                        return fallback;
                    }
            ));
        }
    }

    public Map<Integer,PromotionPriceDto> getListOfPromotionPrice(List<ProductVariant> productVariants, String userId, Integer membershipLevel){
        List<ProductPromotionDto> productPromotionDtos = productVariants.stream().map(pv -> toProductPromotionDto(pv, userId, membershipLevel)).collect(Collectors.toList());
        try {
            Map<Integer,PromotionPriceDto> promotionPrice = promotionWebClient.post()
                    .uri("/api/promotion/internal/check-promotion-price/batch")
                    .bodyValue(productPromotionDtos).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer,PromotionPriceDto>>() {
                    }).block();
            return promotionPrice;
        } catch (Exception e) {
            log.error("Failed to fetch promotion prices batch from promotion-service: {}", e.getMessage());
            return productVariants.stream().collect(Collectors.toMap(
                    ProductVariant::getId,
                    pv -> {
                        PromotionPriceDto fallback = new PromotionPriceDto(pv.getPrice());
                        fallback.setAppliedPromotionType("SERVICE_DOWN");
                        return fallback;
                    }
            ));
        }
    }

    public Map<Integer,PromotionPriceDto> getListOfPromotionPrice(List<ProductVariant> productVariants){
        return getListOfPromotionPrice(productVariants, null, 0);
    }
    public ProductPromotionDto toProductPromotionDto(ProductVariant productVariant, String userId, Integer membershipLevel) {
        Product product = productVariant.getProduct();
        return ProductPromotionDto.builder()
                .productVariantId(productVariant.getId())
                .brandId(product.getBrand()!=null?product.getBrand().getId():null)
                .productId(product.getId())
                .categoryIds(product.getCategories().stream().map(ProductCategory::getId).collect(Collectors.toList()))
                .price(productVariant.getPrice())
                .userId(userId)
                .membershipLevel(membershipLevel)
                .build();
    }
}
