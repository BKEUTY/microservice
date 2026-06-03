package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReplyRequest;
import com.bkeuty.review_service.dto.ReplyResponse;
import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.service.ReviewService;
import com.bkeuty.review_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.review_service.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Validated
public class AdminReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "hasImage", required = false) Boolean hasImage,
            @RequestParam(name = "isReplied", required = false) Boolean isReplied,
            @RequestParam(name = "isHidden", required = false) Boolean isHidden,
            @RequestParam(name = "variantId", required = false) Long variantId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(reviewService.getAllReviewsForAdmin(rating, hasImage, isReplied, isHidden, variantId, pageable));
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReplyResponse> replyToReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "reviewId") Long reviewId,
            @RequestBody ReplyRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return ResponseEntity.ok(reviewService.replyToReview(tokenValidationResponseDto.getUserId(), reviewId, request));
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "replyId") Long replyId,
            @RequestBody ReplyRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return ResponseEntity.ok(reviewService.updateReply(tokenValidationResponseDto.getUserId(), replyId, request));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "replyId") Long replyId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        reviewService.deleteReply(tokenValidationResponseDto.getUserId(), replyId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable(name = "reviewId") Long reviewId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }
}
