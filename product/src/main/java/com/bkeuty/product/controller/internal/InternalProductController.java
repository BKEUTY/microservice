package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.internal.UpdateRatingRequestDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkeuty.product.dto.auth.TokenValidationResponseDto;
import com.bkeuty.product.service.authservice.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/internal")
public class InternalProductController {

    private final ProductVariantRepository productVariantRepository;
    private final AuthService authService;

    @Autowired
    public InternalProductController(ProductVariantRepository productVariantRepository, AuthService authService) {
        this.productVariantRepository = productVariantRepository;
        this.authService = authService;
    }

    @PostMapping("/update-rating")
    public ResponseEntity<Void> updateRating(@RequestBody UpdateRatingRequestDto request) {
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
        
        variant.setAverageRating(request.getAverageRating());
        variant.setReviewCount(request.getReviewCount());
        
        productVariantRepository.save(variant);
        return ResponseEntity.ok().build();
    }
}
