package com.bkeuty.product.microservicecommunication;

import com.bkeuty.product.dto.user.product.ProductPromotionDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.Product;
import com.bkeuty.product.entity.ProductCategory;
import com.bkeuty.product.entity.ProductVariant;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromotionService {
    private final WebClient promotionWebClient;
    public PromotionService(WebClient promotionWebClient) {
        this.promotionWebClient = promotionWebClient;
    }
    public PromotionPriceDto getPromotionPrice(ProductVariant productVariant){

        PromotionPriceDto promotionPriceDto = promotionWebClient.post()
                .uri("/api/promotion/internal/check-promotion-price")
                .bodyValue(toProductPromotionDto(productVariant)).retrieve().bodyToMono(PromotionPriceDto.class).block();
        return  promotionPriceDto;
    }
    public Map<Integer,PromotionPriceDto> getListOfPromotionPrice(Page<ProductVariant> productVariants){
        List<ProductPromotionDto> productPromotionDtos = productVariants.getContent().stream().map(this::toProductPromotionDto).collect(Collectors.toList());
        Map<Integer,PromotionPriceDto> promotionPrice = promotionWebClient.post()
                .uri("/api/promotion/internal/check-promotion-price/batch")
                .bodyValue(productPromotionDtos).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer,PromotionPriceDto>>() {
                }).block();
        return promotionPrice;
    }
    public Map<Integer,PromotionPriceDto> getListOfPromotionPrice(List<ProductVariant> productVariants){
        List<ProductPromotionDto> productPromotionDtos = productVariants.stream().map(this::toProductPromotionDto).collect(Collectors.toList());
        Map<Integer,PromotionPriceDto> promotionPrice = promotionWebClient.post()
                .uri("/api/promotion/internal/check-promotion-price/batch")
                .bodyValue(productPromotionDtos).retrieve().bodyToMono(new ParameterizedTypeReference<Map<Integer,PromotionPriceDto>>() {
                }).block();
        return promotionPrice;
    }
    public ProductPromotionDto toProductPromotionDto(ProductVariant productVariant) {
        Product product = productVariant.getProduct();
        return ProductPromotionDto.builder()
                .productVariantId(productVariant.getId())
                .brandId(product.getBrand()!=null?product.getBrand().getId():null)
                .productId(product.getId())
                .categoryIds(product.getCategories().stream().map(ProductCategory::getId).collect(Collectors.toList()))
                .price(productVariant.getPrice())
                .build();
    }
}
