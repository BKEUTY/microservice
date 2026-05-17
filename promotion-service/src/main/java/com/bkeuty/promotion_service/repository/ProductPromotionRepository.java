package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {
    @Query("""
        SELECT p from ProductPromotion p
        WHERE p.status = com.bkeuty.promotion_service.enums.PromotionStatus.STARTING
        AND :now BETWEEN p.startAt AND p.endAt
        AND (p.productIds IS EMPTY OR :productId MEMBER OF p.productIds)
        AND (p.brandIds IS EMPTY OR (:brandId IS NOT NULL AND :brandId MEMBER OF p.brandIds))
        AND (p.categoryIds IS EMPTY OR EXISTS (SELECT 1 FROM p.categoryIds c WHERE c IN :categoryIds))
        """)
    List<ProductPromotion> findApplicablePromotions(
            @Param("productId") Integer productId,
            @Param("brandId") Integer brandId,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT p from ProductPromotion p
        WHERE p.status = com.bkeuty.promotion_service.enums.PromotionStatus.STARTING
        AND :now BETWEEN p.startAt AND p.endAt
        """)
    List<ProductPromotion> findAllActivePromotions(@Param("now") LocalDateTime now);
}
