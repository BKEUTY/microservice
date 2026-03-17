package com.bkeuty.promotion_service.service.CreatePromotionService;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;

public interface PromotionStrategy {
    String getSupportedType();
    CreatePromotionResponse create(CreatePromotionRequest request);
    CreatePromotionResponse update(Integer promotionId,CreatePromotionRequest request);
}
