package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionResponse;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class ProductPromotionStrategy implements PromotionStrategy {
    private final ProductPromotionRepository productPromotionRepository;
    ProductPromotionStrategy(ProductPromotionRepository productPromotionRepository) {
        this.productPromotionRepository = productPromotionRepository;
    }
    @Override
    public String getSupportedType() {
        return "PRODUCT";
    }

    @Override
    public CreatePromotionResponse create(CreatePromotionRequest request) {
        CreateProductPromotionRequest productReq = (CreateProductPromotionRequest) request;
        ProductPromotion promotion = new ProductPromotion();
        promotion.setTitle(productReq.getTitle());
        promotion.setDescription(productReq.getDescription());
        promotion.setStartAt(productReq.getStartAt());
        promotion.setEndAt(productReq.getEndAt());

        // Set audit fields and default status
        promotion.setCreateAt(LocalDateTime.now());
        promotion.setUpdateAt(LocalDateTime.now());
        promotion.setStatus(PromotionStatus.INCOMING); // Assuming ACTIVE is an enum value you have

        // 3. Map Child Fields
        promotion.setCategoryIds(productReq.getCategoryIds());
        promotion.setProductIds(productReq.getProductIds());
        promotion.setBrandIds(productReq.getBrandIds());
        promotion.setDiscountType(productReq.getDiscountType());
        promotion.setDiscountValue(productReq.getDiscountValue());
        promotion.setMaxDiscount(productReq.getMaxDiscount());
        // 4. Save to database
        // Hibernate handles inserting into the main table AND the three @ElementCollection tables
        return toCreateProductResponseDTO(productPromotionRepository.save(promotion));
    }
    public CreateProductPromotionResponse toCreateProductResponseDTO(ProductPromotion promotion) {
        return CreateProductPromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .startAt(promotion.getStartAt())
                .endAt(promotion.getEndAt())
                .brandIds(promotion.getBrandIds())
                .categoryIds(promotion.getCategoryIds())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .productIds(promotion.getProductIds())
                .maxDiscountValue(promotion.getMaxDiscount())
                .build();
    }
    @Override
    public CreatePromotionResponse update(Integer promotionId, CreatePromotionRequest request){
        CreateProductPromotionRequest productReq = (CreateProductPromotionRequest) request;
        ProductPromotion existingPromotion = productPromotionRepository.findById(promotionId).orElseThrow(()-> new EntityNotFoundException("Promotion with ID:" +promotionId+ " not found"));
        // 2. Update Parent Fields
        if (productReq.getTitle() != null) {
            existingPromotion.setTitle(productReq.getTitle());
        }
        if (productReq.getDescription() != null) {
            existingPromotion.setDescription(productReq.getDescription());
        }
        if (productReq.getStartAt() != null) {
            existingPromotion.setStartAt(productReq.getStartAt());
        }
        if (productReq.getEndAt() != null) {
            existingPromotion.setEndAt(productReq.getEndAt());
        }
        if (productReq.getStatus() != null) {
            existingPromotion.setStatus(productReq.getStatus());
        }

        // Always update the modified timestamp
        existingPromotion.setUpdateAt(LocalDateTime.now());

        // 2. Only update Child Fields if they are provided
        if (productReq.getDiscountType() != null) {
            existingPromotion.setDiscountType(productReq.getDiscountType());
        }
        if (productReq.getDiscountValue() != null) {
            existingPromotion.setDiscountValue(productReq.getDiscountValue());
        }
        if (productReq.getMaxDiscount() != null) {
            existingPromotion.setMaxDiscount(productReq.getMaxDiscount());
        }

        // 3. Update Collections Safely (See updated helper method below)
        updateCollectionSafely(existingPromotion.getCategoryIds(), productReq.getCategoryIds());
        updateCollectionSafely(existingPromotion.getProductIds(), productReq.getProductIds());
        updateCollectionSafely(existingPromotion.getBrandIds(), productReq.getBrandIds());

        // 4. Save and Map to Response
        return toCreateProductResponseDTO(productPromotionRepository.save(existingPromotion));
    }
    private void updateCollectionSafely(Set<Integer> existingCollection, Set<Integer> incomingCollection) {
        if (incomingCollection == null) {
            return;
        }
        existingCollection.clear(); // Hibernate tracks this and deletes old rows in the DB
        if (!incomingCollection.isEmpty()) {
            existingCollection.addAll(incomingCollection); // Hibernate inserts the new rows
        }
    }
}
