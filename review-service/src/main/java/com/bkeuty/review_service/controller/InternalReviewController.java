package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.entity.Review;
import com.bkeuty.review_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review/internal")
public class InternalReviewController {

    private final ReviewRepository reviewRepository;

    @Autowired
    public InternalReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable(name = "userId") String userId) {
        return ResponseEntity.ok(reviewRepository.findByUserId(userId));
    }
}
