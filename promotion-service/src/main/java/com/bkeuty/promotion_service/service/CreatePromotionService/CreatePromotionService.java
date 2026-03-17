package com.bkeuty.promotion_service.service.CreatePromotionService;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateProductPromotionResponse;
import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.ProductPromotionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreatePromotionService {
    private ProductPromotionRepository productPromotionRepository;
    public CreatePromotionService(ProductPromotionRepository productPromotionRepository) {
        this.productPromotionRepository = productPromotionRepository;
    }

    public CreateProductPromotionResponse create(CreateProductPromotionRequest request) {
        ProductPromotion promotion = new ProductPromotion();
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setStartAt(request.getStartAt());
        promotion.setEndAt(request.getEndAt());

        // Set audit fields and default status
        promotion.setCreateAt(LocalDateTime.now());
        promotion.setUpdateAt(LocalDateTime.now());
        promotion.setStatus(PromotionStatus.INCOMING); // Assuming ACTIVE is an enum value you have

        // 3. Map Child Fields
        promotion.setCategoryIds(request.getCategoryIds());
        promotion.setProductIds(request.getProductIds());
        promotion.setBrandIds(request.getBrandIds());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());

        // 4. Save to database
        // Hibernate handles inserting into the main table AND the three @ElementCollection tables
        return create(promotion);
    }
    public CreateProductPromotionResponse create(ProductPromotion promotion) {
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
                .productIds(promotion.getProductIds()).build();
    }
}
