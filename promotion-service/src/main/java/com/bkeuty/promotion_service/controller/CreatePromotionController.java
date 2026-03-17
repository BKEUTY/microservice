package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.service.CreatePromotionService.PromotionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promotion")
public class CreatePromotionController {

    private final PromotionFactory  promotionFactory;
    public CreatePromotionController(PromotionFactory promotionFactory) {
        this.promotionFactory = promotionFactory;
    }
    @PostMapping
    public ResponseEntity<CreatePromotionResponse> createPromotion(@RequestBody CreatePromotionRequest request) {

        CreatePromotionResponse response = promotionFactory.executeCreation(request);

        // Return 201 Created status code along with the response body
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<CreatePromotionResponse> updatePromotion(@PathVariable Integer id,@RequestBody CreatePromotionRequest request) {
        CreatePromotionResponse response = promotionFactory.executeUpdate(id,request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(response);
    }
}
