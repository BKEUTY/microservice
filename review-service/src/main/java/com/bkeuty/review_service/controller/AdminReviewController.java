package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReplyRequest;
import com.bkeuty.review_service.dto.ReplyResponse;
import com.bkeuty.review_service.service.ReviewService;
import com.bkeuty.review_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.review_service.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReplyResponse> replyToReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long reviewId,
            @RequestBody ReplyRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(reviewService.replyToReview(tokenValidationResponseDto.getUserId(), reviewId, request));
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long replyId,
            @RequestBody ReplyRequest request) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(reviewService.updateReply(tokenValidationResponseDto.getUserId(), replyId, request));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long replyId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        reviewService.deleteReply(tokenValidationResponseDto.getUserId(), replyId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @PathVariable Long reviewId) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if (tokenValidationResponseDto.getUserId() == null || tokenValidationResponseDto.getUserRole() == null
                || !"ADMIN".equalsIgnoreCase(tokenValidationResponseDto.getUserRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }
}
