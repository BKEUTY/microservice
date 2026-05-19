package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.ProductPromotion;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductPromotionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductPromotionRepository productPromotionRepository;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 19, 12, 0);

    @BeforeEach
    void seedData() {
        // Promotion 1: STARTING, valid time, specific product (10), brand (1), category (100)
        ProductPromotion p1 = new ProductPromotion();
        p1.setTitle("Khuyen mai 1");
        p1.setDescription("Valid promotion");
        p1.setStartAt(NOW.minusDays(2));
        p1.setEndAt(NOW.plusDays(2));
        p1.setStatus(PromotionStatus.STARTING);
        p1.setDiscountType(DiscountType.PERCENTAGE);
        p1.setDiscountValue(20);
        p1.setProductIds(Set.of(10, 11));
        p1.setBrandIds(Set.of(1, 2));
        p1.setCategoryIds(Set.of(100, 101));
        entityManager.persist(p1);

        // Promotion 2: DISABLED, valid time, specific product (10)
        ProductPromotion p2 = new ProductPromotion();
        p2.setTitle("Khuyen mai 2");
        p2.setDescription("Disabled promotion");
        p2.setStartAt(NOW.minusDays(2));
        p2.setEndAt(NOW.plusDays(2));
        p2.setStatus(PromotionStatus.DISABLED);
        p2.setDiscountType(DiscountType.AMOUNT);
        p2.setDiscountValue(50000);
        p2.setProductIds(Set.of(10));
        entityManager.persist(p2);

        // Promotion 3: STARTING, expired (endAt in the past)
        ProductPromotion p3 = new ProductPromotion();
        p3.setTitle("Khuyen mai 3");
        p3.setDescription("Expired promotion");
        p3.setStartAt(NOW.minusDays(5));
        p3.setEndAt(NOW.minusDays(1));
        p3.setStatus(PromotionStatus.STARTING);
        p3.setDiscountType(DiscountType.PERCENTAGE);
        p3.setDiscountValue(10);
        p3.setProductIds(Set.of(10));
        entityManager.persist(p3);

        // Promotion 4: STARTING, valid time, broad eligibility (empty restrictions)
        ProductPromotion p4 = new ProductPromotion();
        p4.setTitle("Khuyen mai 4");
        p4.setDescription("Broad eligibility");
        p4.setStartAt(NOW.minusDays(1));
        p4.setEndAt(NOW.plusDays(1));
        p4.setStatus(PromotionStatus.STARTING);
        p4.setDiscountType(DiscountType.PERCENTAGE);
        p4.setDiscountValue(15);
        p4.setProductIds(Set.of());
        p4.setBrandIds(Set.of());
        p4.setCategoryIds(Set.of());
        entityManager.persist(p4);

        entityManager.flush();
    }

    @Test
    void findApplicablePromotions_ShouldReturnEligiblePromotions() {
        List<ProductPromotion> list = productPromotionRepository.findApplicablePromotions(
                10, 1, List.of(100), NOW);

        assertNotNull(list);
        assertEquals(2, list.size(), "Should match specific p1 and broad p4");
        assertTrue(list.stream().anyMatch(p -> p.getTitle().equals("Khuyen mai 1")));
        assertTrue(list.stream().anyMatch(p -> p.getTitle().equals("Khuyen mai 4")));
    }

    @Test
    void findApplicablePromotions_ShouldExcludeNonMatchingBrand() {
        // Brand 99 does not match p1, but matches p4 (since p4 has no brand restrictions)
        List<ProductPromotion> list = productPromotionRepository.findApplicablePromotions(
                10, 99, List.of(100), NOW);

        assertNotNull(list);
        assertEquals(1, list.size(), "Should only match broad p4");
        assertEquals("Khuyen mai 4", list.get(0).getTitle());
    }

    @Test
    void findApplicablePromotions_ShouldExcludeNonMatchingProduct() {
        // Product 99 does not match p1, but matches p4 (since p4 has no product restrictions)
        List<ProductPromotion> list = productPromotionRepository.findApplicablePromotions(
                99, 1, List.of(100), NOW);

        assertNotNull(list);
        assertEquals(1, list.size(), "Should only match broad p4");
        assertEquals("Khuyen mai 4", list.get(0).getTitle());
    }

    @Test
    void findApplicablePromotions_ShouldExcludeNonMatchingCategory() {
        // Category 999 does not match p1, but matches p4 (since p4 has no category restrictions)
        List<ProductPromotion> list = productPromotionRepository.findApplicablePromotions(
                10, 1, List.of(999), NOW);

        assertNotNull(list);
        assertEquals(1, list.size(), "Should only match broad p4");
        assertEquals("Khuyen mai 4", list.get(0).getTitle());
    }
}
