package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReviewPageResponse;
import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicReviewController.class)
class PublicReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void getReviewsByProduct_ShouldReturn200_WithReviewList() throws Exception {
        ReviewResponse review = new ReviewResponse();
        review.setId(1L);
        review.setUserName("Nguyen A");
        review.setRating(5);
        review.setComment("Tuyet voi");

        Page<ReviewResponse> page = new PageImpl<>(List.of(review));
        ReviewPageResponse pageResponse = ReviewPageResponse.builder()
                .reviews(page)
                .ratingCounts(Map.of("5", 1L))
                .build();

        when(reviewService.getReviewsByVariantId(eq(101L), any(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/reviews/product/101")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews.content", hasSize(1)))
                .andExpect(jsonPath("$.reviews.content[0].userName", is("Nguyen A")))
                .andExpect(jsonPath("$.reviews.content[0].rating", is(5)));
    }

    @Test
    void getReviewStats_ShouldReturn200_WithRatingBreakdown() throws Exception {
        Map<String, Long> stats = Map.of("5", 10L, "4", 5L, "3", 2L);
        when(reviewService.getReviewStats(eq(101L))).thenReturn(stats);

        mockMvc.perform(get("/api/reviews/product/101/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.5", is(10)))
                .andExpect(jsonPath("$.4", is(5)));
    }

    @Test
    void healthCheck_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reviews/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
