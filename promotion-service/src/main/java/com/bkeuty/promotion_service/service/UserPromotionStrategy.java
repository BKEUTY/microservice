package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateUserPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateUserPromotionResponse;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.entity.UserPromotion;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.UserPromotionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserPromotionStrategy implements PromotionStrategy {
    private final UserPromotionRepository userPromotionRepository;

    public UserPromotionStrategy(UserPromotionRepository userPromotionRepository) {
        this.userPromotionRepository = userPromotionRepository;
    }

    @Override
    public String getSupportedType() {
        return "USER";
    }

    @Override
    public CreatePromotionResponse create(CreatePromotionRequest request) {
        CreateUserPromotionRequest userReq = (CreateUserPromotionRequest) request;
        UserPromotion promotion = new UserPromotion();
        
        mapCommonFields(promotion, userReq);
        
        promotion.setBirthdayMonth(userReq.getBirthdayMonth());
        promotion.setMembershipLevels(userReq.getMembershipLevels());
        promotion.setUserIds(userReq.getUserIds());
        
        return toResponseDTO(userPromotionRepository.save(promotion));
    }

    @Override
    public CreatePromotionResponse update(Integer promotionId, CreatePromotionRequest request) {
        CreateUserPromotionRequest userReq = (CreateUserPromotionRequest) request;
        UserPromotion existing = userPromotionRepository.findById(promotionId)
                .orElseThrow(() -> new EntityNotFoundException("User Promotion with ID: " + promotionId + " not found"));
        
        updateCommonFields(existing, userReq);
        
        updateCollectionSafely(existing.getBirthdayMonth(), userReq.getBirthdayMonth());
        if (userReq.getMembershipLevels() != null) {
            if (existing.getMembershipLevels() == null) {
                existing.setMembershipLevels(new java.util.HashSet<>());
            }
            existing.getMembershipLevels().clear();
            existing.getMembershipLevels().addAll(userReq.getMembershipLevels());
        }
        if (userReq.getUserIds() != null) {
            existing.getUserIds().clear();
            existing.getUserIds().addAll(userReq.getUserIds());
        }
        
        return toResponseDTO(userPromotionRepository.save(existing));
    }

    private void mapCommonFields(UserPromotion promotion, CreateUserPromotionRequest req) {
        promotion.setTitle(req.getTitle());
        promotion.setDescription(req.getDescription());
        promotion.setStartAt(req.getStartAt());
        promotion.setEndAt(req.getEndAt());
        promotion.setStatus(req.getStatus() != null ? req.getStatus() : PromotionStatus.INCOMING);
        promotion.setDiscountType(req.getDiscountType());
        promotion.setDiscountValue(req.getDiscountValue());
        promotion.setMaxDiscount(req.getMaxDiscount());
    }

    private void updateCommonFields(UserPromotion promotion, CreateUserPromotionRequest req) {
        if (req.getTitle() != null) promotion.setTitle(req.getTitle());
        if (req.getDescription() != null) promotion.setDescription(req.getDescription());
        if (req.getStartAt() != null) promotion.setStartAt(req.getStartAt());
        if (req.getEndAt() != null) promotion.setEndAt(req.getEndAt());
        if (req.getStatus() != null) promotion.setStatus(req.getStatus());
        if (req.getDiscountType() != null) promotion.setDiscountType(req.getDiscountType());
        if (req.getDiscountValue() != null) promotion.setDiscountValue(req.getDiscountValue());
        if (req.getMaxDiscount() != null) promotion.setMaxDiscount(req.getMaxDiscount());
    }

    private void updateCollectionSafely(Set<Integer> existing, Set<Integer> incoming) {
        if (incoming == null) return;
        existing.clear();
        existing.addAll(incoming);
    }

    private CreateUserPromotionResponse toResponseDTO(UserPromotion promotion) {
        return CreateUserPromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .startAt(promotion.getStartAt())
                .endAt(promotion.getEndAt())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .birthdayMonth(promotion.getBirthdayMonth())
                .membershipLevels(promotion.getMembershipLevels())
                .userIds(promotion.getUserIds())
                .build();
    }
}
