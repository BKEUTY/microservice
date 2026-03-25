package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.promotion_service.service.AuthService;
import com.bkeuty.promotion_service.service.PromotionFactory;
import com.bkeuty.promotion_service.service.PromotionService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promotion")
public class PromotionController {
    private final AuthService authService;
    private final PromotionService promotionService;
    private final PromotionFactory promotionFactory;

    public PromotionController(PromotionFactory promotionFactory, AuthService authService,
            PromotionService promotionService) {
        this.promotionFactory = promotionFactory;
        this.authService = authService;
        this.promotionService = promotionService;
    }

    @PostMapping
    public ResponseEntity<CreatePromotionResponse> createPromotion(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody CreatePromotionRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CreatePromotionResponse response = promotionFactory.executeCreation(request);

        // Return 201 Created status code along with the response body
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreatePromotionResponse> updatePromotion(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Integer id, @RequestBody CreatePromotionRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CreatePromotionResponse response = promotionFactory.executeUpdate(id, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponseDto>> getPromotion(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(defaultValue = "0") int page) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"admin".equals(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Pageable pageable = PageRequest.of(page, 30);
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(promotionService.findAll(pageable));
    }
}
