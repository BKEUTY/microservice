package com.bkeuty.review_service.service;

import com.bkeuty.review_service.dto.*;
import com.bkeuty.review_service.entity.AdminReply;
import com.bkeuty.review_service.entity.Review;
import com.bkeuty.review_service.repository.AdminReplyRepository;
import com.bkeuty.review_service.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.review_service.microservicecommunication.OrderService;
import com.bkeuty.review_service.microservicecommunication.ProductService;
import com.bkeuty.review_service.microservicecommunication.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AdminReplyRepository adminReplyRepository;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public ReviewResponse createReview(String userId, ReviewRequest request, String token) {
        Boolean isDelivered = checkOrderIsDelivered(userId, request.getVariantId(), token);
        if (Boolean.FALSE.equals(isDelivered)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User has not purchased this product or order is not delivered.");
        }
        String userName = userService.getUserName(userId);

        Review review = Review.builder()
                .userId(userId)
                .userName(userName)
                .variantId(request.getVariantId())
                .rating(request.getRating())
                .comment(request.getComment())
                .images(request.getImages())
                .isHidden(false)
                .build();

        review = reviewRepository.save(review);
        updateProductRating(request.getVariantId());
        orderService.markReviewed(userId, request.getVariantId().intValue());

        return mapToReviewResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(String userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Review not found or not owned by user"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImages(request.getImages());
        review.setUserName(userService.getUserName(userId));

        review = reviewRepository.save(review);
        updateProductRating(review.getVariantId());

        return mapToReviewResponse(review);
    }

    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Review not found or not owned by user"));

        review.setHidden(true);
        if (review.getAdminReply() != null) {
            adminReplyRepository.delete(review.getAdminReply());
            review.setAdminReply(null);
            review.setReplied(false);
        }
        reviewRepository.save(review);
        updateProductRating(review.getVariantId());
    }

    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setHidden(true);
        if (review.getAdminReply() != null) {
            adminReplyRepository.delete(review.getAdminReply());
            review.setAdminReply(null);
            review.setReplied(false);
        }
        reviewRepository.save(review);
        updateProductRating(review.getVariantId());
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        return "https://res.cloudinary.com/demo/image/upload/v1/reviews/" + UUID.randomUUID() + "-"
                + file.getOriginalFilename();
    }

    @Transactional
    public ReplyResponse replyToReview(String adminId, Long reviewId, ReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (review.getAdminReply() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review already has a reply");
        }
        String adminName = userService.getUserName(adminId);

        AdminReply reply = AdminReply.builder()
                .review(review)
                .comment(request.getComment())
                .adminId(adminId)
                .adminName(adminName)
                .build();

        reply = adminReplyRepository.save(reply);
        review.setReplied(true);
        reviewRepository.save(review);
        
        return mapToReplyResponse(reply);
    }

    @Transactional
    public ReplyResponse updateReply(String adminId, Long replyId, ReplyRequest request) {
        AdminReply reply = adminReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));

        reply.setComment(request.getComment());
        reply.setAdminId(adminId);
        reply.setAdminName(userService.getUserName(adminId));
        reply = adminReplyRepository.save(reply);

        return mapToReplyResponse(reply);
    }

    @Transactional
    public void deleteReply(String adminId, Long replyId) {
        AdminReply reply = adminReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));
        
        Review review = reply.getReview();
        review.setReplied(false);
        review.setAdminReply(null);
        reviewRepository.save(review);
        adminReplyRepository.delete(reply);
    }

    public Map<String, Long> getReviewStats(Long variantId) {
        Map<String, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(String.valueOf(i), 0L);
        }

        List<Object[]> counts = reviewRepository.countRatingByVariantId(variantId);
        for (Object[] obj : counts) {
            ratingCounts.put(String.valueOf(obj[0]), (Long) obj[1]);
        }
        return ratingCounts;
    }

    public ReviewPageResponse getReviewsByVariantId(Long variantId, Integer rating, Boolean hasImage, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByFilters(variantId, rating, hasImage, pageable);

        return ReviewPageResponse.builder()
                .reviews(reviewPage.map(r -> mapToReviewResponse(r)))
                .ratingCounts(getReviewStats(variantId))
                .build();
    }

    public Page<ReviewResponse> getAllReviewsForAdmin(Integer rating, Boolean hasImage, Boolean isReplied, Boolean isHidden, Long variantId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByAdminFilters(rating, hasImage, isReplied, isHidden, variantId, pageable);
        return reviewPage.map(this::mapToReviewResponse);
    }

    private Boolean checkOrderIsDelivered(String userId, Long variantId, String token) {
        try {
            return orderService.checkOrderIsDelivered(userId, variantId.intValue());
        } catch (Exception e) {
            log.warn("Could not verify order status with OrderService. Fallback to false. Error: {}",
                    e.getMessage());
            return false;
        }
    }

    private void updateProductRating(Long variantId) {
        try {
            Double averageRating = reviewRepository.getAverageRating(variantId);
            Long reviewCount = reviewRepository.getReviewCount(variantId);
            
            if (averageRating == null) {
                averageRating = 0.0;
            }
            if (reviewCount == null) {
                reviewCount = 0L;
            }
            
            productService.updateProductRating(variantId.intValue(), averageRating, reviewCount.intValue());
            log.info("Successfully updated product rating for variant {}", variantId);
        } catch (Exception e) {
            log.error("Error updating product rating", e);
        }
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        ReplyResponse replyRes = review.getAdminReply() != null ? mapToReplyResponse(review.getAdminReply()) : null;

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(review.getUserName() != null ? review.getUserName() : "User " + review.getUserId())
                .variantId(review.getVariantId())
                .rating(review.getRating())
                .comment(review.getComment())
                .images(review.getImages())
                .isHidden(review.isHidden())
                .isReplied(review.isReplied())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reply(replyRes)
                .build();
    }

    private ReplyResponse mapToReplyResponse(AdminReply reply) {
        String displayName = reply.getAdminName() != null ? reply.getAdminName() : "BKEUTY Admin";

        return ReplyResponse.builder()
                .id(reply.getId())
                .adminId(reply.getAdminId())
                .adminName(displayName)
                .comment(reply.getComment())
                .repliedAt(reply.getRepliedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
