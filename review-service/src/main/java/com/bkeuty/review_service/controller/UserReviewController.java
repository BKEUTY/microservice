package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReviewRequest;
import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.service.ReviewService;
import com.bkeuty.review_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.review_service.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody ReviewRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"USER".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login to submit a review");
        }
        String pureToken = bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        return ResponseEntity.ok(reviewService.createReview(tokenValidationResponseDto.getUserId(), request, pureToken));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"USER".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login to update your review");
        }
        return ResponseEntity.ok(reviewService.updateReview(tokenValidationResponseDto.getUserId(), reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long reviewId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"USER".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login to delete your review");
        }
        reviewService.deleteReview(tokenValidationResponseDto.getUserId(), reviewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam("file") MultipartFile file) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"USER".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login to upload images");
        }
        return ResponseEntity.ok(reviewService.uploadImage(file));
    }
}
