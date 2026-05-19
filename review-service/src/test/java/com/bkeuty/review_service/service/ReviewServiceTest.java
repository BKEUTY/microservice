package com.bkeuty.review_service.service;

import com.bkeuty.review_service.dto.ReplyRequest;
import com.bkeuty.review_service.dto.ReplyResponse;
import com.bkeuty.review_service.dto.ReviewRequest;
import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.entity.AdminReply;
import com.bkeuty.review_service.entity.Review;
import com.bkeuty.review_service.microservicecommunication.OrderService;
import com.bkeuty.review_service.microservicecommunication.ProductService;
import com.bkeuty.review_service.microservicecommunication.UserService;
import com.bkeuty.review_service.repository.AdminReplyRepository;
import com.bkeuty.review_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private AdminReplyRepository adminReplyRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewService reviewService;

    private static final String USER_ID = "user-uuid-123";
    private static final String ADMIN_ID = "admin-uuid-456";
    private static final Long VARIANT_ID = 10L;
    private static final Long REVIEW_ID = 1L;

    private Review mockReview;

    @BeforeEach
    void setUp() {
        mockReview = Review.builder()
                .id(REVIEW_ID)
                .userId(USER_ID)
                .userName("John Doe")
                .variantId(VARIANT_ID)
                .rating(5)
                .comment("Great product!")
                .images(Collections.emptyList())
                .isHidden(false)
                .build();
    }

    @Test
    void createReview_ShouldSaveAndReturnDto_WhenOrderIsDelivered() {
        ReviewRequest request = new ReviewRequest();
        request.setVariantId(VARIANT_ID);
        request.setRating(5);
        request.setComment("Great product!");
        request.setImages(Collections.emptyList());

        when(orderService.checkOrderIsDelivered(USER_ID, VARIANT_ID.intValue())).thenReturn(true);
        when(userService.getUserName(USER_ID)).thenReturn("John Doe");
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewRepository.getAverageRating(VARIANT_ID)).thenReturn(5.0);
        when(reviewRepository.getReviewCount(VARIANT_ID)).thenReturn(1L);
        doNothing().when(productService).updateProductRating(anyInt(), anyDouble(), anyInt());
        doNothing().when(orderService).markReviewed(any(), anyInt());

        ReviewResponse response = reviewService.createReview(USER_ID, request, "token");

        assertNotNull(response);
        assertEquals(REVIEW_ID, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("Great product!", response.getComment());

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(orderService, times(1)).markReviewed(USER_ID, VARIANT_ID.intValue());
    }

    @Test
    void createReview_ShouldThrowForbidden_WhenOrderNotDelivered() {
        ReviewRequest request = new ReviewRequest();
        request.setVariantId(VARIANT_ID);

        when(orderService.checkOrderIsDelivered(USER_ID, VARIANT_ID.intValue())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reviewService.createReview(USER_ID, request, "token"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_ShouldUpdateAndReturnDto_WhenReviewExists() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(4);
        request.setComment("Updated comment");
        request.setImages(Collections.emptyList());

        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(mockReview));
        when(userService.getUserName(USER_ID)).thenReturn("John Doe");
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewRepository.getAverageRating(VARIANT_ID)).thenReturn(4.5);
        when(reviewRepository.getReviewCount(VARIANT_ID)).thenReturn(2L);
        doNothing().when(productService).updateProductRating(anyInt(), anyDouble(), anyInt());

        ReviewResponse response = reviewService.updateReview(USER_ID, REVIEW_ID, request);

        assertNotNull(response);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void updateReview_ShouldThrowNotFound_WhenReviewNotOwned() {
        ReviewRequest request = new ReviewRequest();
        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reviewService.updateReview(USER_ID, REVIEW_ID, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteReview_ShouldHideReview_WhenReviewExists() {
        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(mockReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewRepository.getAverageRating(VARIANT_ID)).thenReturn(0.0);
        when(reviewRepository.getReviewCount(VARIANT_ID)).thenReturn(0L);
        doNothing().when(productService).updateProductRating(anyInt(), anyDouble(), anyInt());

        assertDoesNotThrow(() -> reviewService.deleteReview(USER_ID, REVIEW_ID));

        assertTrue(mockReview.isHidden());
        verify(reviewRepository, times(1)).save(mockReview);
    }

    @Test
    void deleteReviewByAdmin_ShouldHideReview_WhenReviewExists() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(mockReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewRepository.getAverageRating(VARIANT_ID)).thenReturn(0.0);
        when(reviewRepository.getReviewCount(VARIANT_ID)).thenReturn(0L);
        doNothing().when(productService).updateProductRating(anyInt(), anyDouble(), anyInt());

        assertDoesNotThrow(() -> reviewService.deleteReviewByAdmin(REVIEW_ID));

        assertTrue(mockReview.isHidden());
    }

    @Test
    void replyToReview_ShouldSaveAndReturnDto_WhenReviewExistsAndNoReply() {
        ReplyRequest request = new ReplyRequest();
        request.setComment("Thank you for your feedback!");

        AdminReply mockReply = AdminReply.builder()
                .id(1L)
                .review(mockReview)
                .adminId(ADMIN_ID)
                .adminName("Admin")
                .comment("Thank you for your feedback!")
                .build();

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(mockReview));
        when(userService.getUserName(ADMIN_ID)).thenReturn("Admin");
        when(adminReplyRepository.save(any(AdminReply.class))).thenReturn(mockReply);
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);

        ReplyResponse response = reviewService.replyToReview(ADMIN_ID, REVIEW_ID, request);

        assertNotNull(response);
        assertEquals("Thank you for your feedback!", response.getComment());
        assertEquals("Admin", response.getAdminName());

        verify(adminReplyRepository, times(1)).save(any(AdminReply.class));
    }

    @Test
    void replyToReview_ShouldThrowBadRequest_WhenReplyAlreadyExists() {
        AdminReply existingReply = AdminReply.builder().id(99L).build();
        mockReview.setAdminReply(existingReply);

        ReplyRequest request = new ReplyRequest();
        request.setComment("Duplicate reply");

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(mockReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reviewService.replyToReview(ADMIN_ID, REVIEW_ID, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(adminReplyRepository, never()).save(any());
    }

    @Test
    void getReviewStats_ShouldReturnRatingCountMap() {
        when(reviewRepository.countRatingByVariantId(VARIANT_ID))
                .thenReturn(List.of(new Object[]{5, 3L}, new Object[]{4, 1L}));

        Map<String, Long> stats = reviewService.getReviewStats(VARIANT_ID);

        assertNotNull(stats);
        assertEquals(5, stats.size()); // Keys 1..5
        assertEquals(3L, stats.get("5"));
        assertEquals(1L, stats.get("4"));
        assertEquals(0L, stats.get("1"));
    }
}
