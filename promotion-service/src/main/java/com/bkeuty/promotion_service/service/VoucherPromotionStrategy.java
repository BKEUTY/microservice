package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.CreateVoucherPromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.CreateVoucherPromotionResponse;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.entity.VoucherPromotion;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.repository.VoucherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VoucherPromotionStrategy implements PromotionStrategy {
    private final VoucherRepository voucherRepository;

    public VoucherPromotionStrategy(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    public String getSupportedType() {
        return "VOUCHER";
    }

    @Override
    public CreatePromotionResponse create(CreatePromotionRequest request) {
        CreateVoucherPromotionRequest voucherReq = (CreateVoucherPromotionRequest) request;
        VoucherPromotion promotion = new VoucherPromotion();
        
        mapCommonFields(promotion, voucherReq);
        
        promotion.setCode(voucherReq.getCode());
        promotion.setTotalQuantity(voucherReq.getTotalQuantity());
        promotion.setRemainingQuantity(voucherReq.getTotalQuantity());
        promotion.setMinOrderValue(voucherReq.getMinOrderValue());
        promotion.setUsageLimitPerUser(voucherReq.getUsageLimitPerUser());
        
        return toResponseDTO(voucherRepository.save(promotion));
    }

    @Override
    public CreatePromotionResponse update(Integer promotionId, CreatePromotionRequest request) {
        CreateVoucherPromotionRequest voucherReq = (CreateVoucherPromotionRequest) request;
        VoucherPromotion existing = voucherRepository.findById(promotionId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher Promotion with ID: " + promotionId + " not found"));
        
        updateCommonFields(existing, voucherReq);
        
        if (voucherReq.getCode() != null) existing.setCode(voucherReq.getCode());
        if (voucherReq.getTotalQuantity() != null) {
            int diff = voucherReq.getTotalQuantity() - existing.getTotalQuantity();
            existing.setTotalQuantity(voucherReq.getTotalQuantity());
            existing.setRemainingQuantity(existing.getRemainingQuantity() + diff);
        }
        if (voucherReq.getMinOrderValue() != null) existing.setMinOrderValue(voucherReq.getMinOrderValue());
        if (voucherReq.getUsageLimitPerUser() != null) existing.setUsageLimitPerUser(voucherReq.getUsageLimitPerUser());
        
        return toResponseDTO(voucherRepository.save(existing));
    }

    private void mapCommonFields(VoucherPromotion promotion, CreateVoucherPromotionRequest req) {
        promotion.setTitle(req.getTitle());
        promotion.setDescription(req.getDescription());
        promotion.setStartAt(req.getStartAt());
        promotion.setEndAt(req.getEndAt());
        promotion.setStatus(req.getStatus() != null ? req.getStatus() : PromotionStatus.INCOMING);
        promotion.setDiscountType(req.getDiscountType());
        promotion.setDiscountValue(req.getDiscountValue());
        promotion.setMaxDiscount(req.getMaxDiscount());
        promotion.setMembershipLevels(req.getMembershipLevels());
    }

    private void updateCommonFields(VoucherPromotion promotion, CreateVoucherPromotionRequest req) {
        if (req.getTitle() != null) promotion.setTitle(req.getTitle());
        if (req.getDescription() != null) promotion.setDescription(req.getDescription());
        if (req.getStartAt() != null) promotion.setStartAt(req.getStartAt());
        if (req.getEndAt() != null) promotion.setEndAt(req.getEndAt());
        if (req.getStatus() != null) promotion.setStatus(req.getStatus());
        if (req.getDiscountType() != null) promotion.setDiscountType(req.getDiscountType());
        if (req.getDiscountValue() != null) promotion.setDiscountValue(req.getDiscountValue());
        if (req.getMaxDiscount() != null) promotion.setMaxDiscount(req.getMaxDiscount());
        if (req.getMembershipLevels() != null) {
            if (promotion.getMembershipLevels() == null) {
                promotion.setMembershipLevels(new java.util.HashSet<>());
            }
            promotion.getMembershipLevels().clear();
            promotion.getMembershipLevels().addAll(req.getMembershipLevels());
        }
    }

    private CreateVoucherPromotionResponse toResponseDTO(VoucherPromotion promotion) {
        return CreateVoucherPromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .startAt(promotion.getStartAt())
                .endAt(promotion.getEndAt())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .code(promotion.getCode())
                .totalQuantity(promotion.getTotalQuantity())
                .remainingQuantity(promotion.getRemainingQuantity())
                .minOrderValue(promotion.getMinOrderValue())
                .usageLimitPerUser(promotion.getUsageLimitPerUser())
                .membershipLevels(promotion.getMembershipLevels())
                .build();
    }
}
