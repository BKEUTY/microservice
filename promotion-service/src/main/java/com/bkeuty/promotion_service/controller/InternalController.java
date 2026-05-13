package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckRequestDTO;
import com.bkeuty.promotion_service.dto.internal.ProductPromotionCheckResponseDTO;
import com.bkeuty.promotion_service.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion/internal")
public class InternalController {
    private final PromotionService promotionService;
    public InternalController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/check-promotion-price/batch")
    public ResponseEntity<Map<Integer, ProductPromotionCheckResponseDTO>> getPromotionPriceBatch (@RequestBody List<ProductPromotionCheckRequestDTO> productPromotionCheckRequestDTOList) {
        return ResponseEntity.ok(promotionService.checkProductPromotion(productPromotionCheckRequestDTOList));
    }
    @PostMapping("/check-promotion-price")
    public ResponseEntity<ProductPromotionCheckResponseDTO> getPromotionPrice (@RequestBody ProductPromotionCheckRequestDTO productPromotionCheckRequestDTO) {
        return ResponseEntity.ok(promotionService.getPromotionPrice(productPromotionCheckRequestDTO));
    }

}
