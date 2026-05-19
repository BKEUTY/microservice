package com.bkeuty.review_service.repository;

import com.bkeuty.review_service.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private static final Long VARIANT_ID = 101L;

    @BeforeEach
    void seedData() {
        // Review 1: 5 stars, with image, visible
        Review r1 = Review.builder()
                .userId("user-A")
                .userName("Nguyen A")
                .variantId(VARIANT_ID)
                .rating(5)
                .comment("San pham tuyet voi!")
                .images(List.of("https://img.bkeuty.com/review1.jpg"))
                .isHidden(false)
                .isReplied(false)
                .build();
        entityManager.persist(r1);

        // Review 2: 4 stars, no image, visible
        Review r2 = Review.builder()
                .userId("user-B")
                .userName("Tran B")
                .variantId(VARIANT_ID)
                .rating(4)
                .comment("Kha tot")
                .isHidden(false)
                .isReplied(true)
                .build();
        entityManager.persist(r2);

        // Review 3: 2 stars, no image, HIDDEN
        Review r3 = Review.builder()
                .userId("user-C")
                .userName("Le C")
                .variantId(VARIANT_ID)
                .rating(2)
                .comment("Khong tot")
                .isHidden(true)
                .isReplied(false)
                .build();
        entityManager.persist(r3);

        // Review 4: 5 stars, different variant
        Review r4 = Review.builder()
                .userId("user-A")
                .userName("Nguyen A")
                .variantId(202L)
                .rating(5)
                .comment("Variant khac")
                .isHidden(false)
                .isReplied(false)
                .build();
        entityManager.persist(r4);

        entityManager.flush();
    }

    // === DATA LAYER: Average Rating and Count ===

    @Test
    void getAverageRating_ShouldCalculateOnlyVisibleReviews() {
        Double avg = reviewRepository.getAverageRating(VARIANT_ID);
        assertNotNull(avg);
        assertEquals(4.5, avg, 0.01,
                "Average of visible reviews (5 + 4) / 2 = 4.5, hidden review excluded");
    }

    @Test
    void getReviewCount_ShouldCountOnlyVisibleReviews() {
        Long count = reviewRepository.getReviewCount(VARIANT_ID);
        assertEquals(2L, count, "Only 2 visible reviews for variant 101");
    }

    @Test
    void countRatingByVariantId_ShouldGroupRatingsCorrectly() {
        List<Object[]> ratingCounts = reviewRepository.countRatingByVariantId(VARIANT_ID);
        assertNotNull(ratingCounts);
        assertEquals(2, ratingCounts.size(), "2 distinct ratings (5 and 4) for visible reviews");
    }

    // === DATA LAYER: Filtered Queries ===

    @Test
    void findByVariantIdAndIsHiddenFalse_ShouldExcludeHiddenReviews() {
        Page<Review> page = reviewRepository.findByVariantIdAndIsHiddenFalse(
                VARIANT_ID, PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements(), "Hidden review should be excluded");
    }

    @Test
    void findByFilters_ShouldFilterByRating() {
        Page<Review> page = reviewRepository.findByFilters(
                VARIANT_ID, 5, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(5, page.getContent().get(0).getRating());
    }

    @Test
    void findByFilters_ShouldFilterByHasImage() {
        Page<Review> page = reviewRepository.findByFilters(
                VARIANT_ID, null, true, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().get(0).getImages().isEmpty());
    }

    @Test
    void findByAdminFilters_ShouldIncludeHiddenReviews_WhenFilterIsNull() {
        Page<Review> page = reviewRepository.findByAdminFilters(
                null, null, null, null, VARIANT_ID, PageRequest.of(0, 10));
        assertEquals(3, page.getTotalElements(),
                "Admin filter with all nulls should return all reviews including hidden");
    }

    @Test
    void findByAdminFilters_ShouldFilterByIsReplied() {
        Page<Review> page = reviewRepository.findByAdminFilters(
                null, null, true, null, VARIANT_ID, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().get(0).isReplied());
    }

    // === EDGE CASES ===

    @Test
    void getAverageRating_ShouldReturnNull_WhenNoReviews() {
        Double avg = reviewRepository.getAverageRating(9999L);
        assertNull(avg, "No reviews for non-existent variant should return null");
    }

    @Test
    void findByUserId_ShouldReturnAllReviewsOfUser() {
        List<Review> reviews = reviewRepository.findByUserId("user-A");
        assertEquals(2, reviews.size(), "User A has 2 reviews across different variants");
    }
}
