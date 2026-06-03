package com.bkeuty.review_service.repository;

import com.bkeuty.review_service.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByVariantIdAndIsHiddenFalse(Long variantId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false " +
           "AND (:rating IS NULL OR r.rating = :rating) " +
           "AND (:hasImage IS NULL OR (:hasImage = true AND r.images IS NOT EMPTY) OR (:hasImage = false AND r.images IS EMPTY))")
    Page<Review> findByFilters(@Param("variantId") Long variantId, @Param("rating") Integer rating, @Param("hasImage") Boolean hasImage, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE " +
           "(:variantId IS NULL OR r.variantId = :variantId) " +
           "AND (:rating IS NULL OR r.rating = :rating) " +
           "AND (:hasImage IS NULL OR (:hasImage = true AND r.images IS NOT EMPTY) OR (:hasImage = false AND r.images IS EMPTY)) " +
           "AND (:isReplied IS NULL OR r.isReplied = :isReplied) " +
           "AND (:isHidden IS NULL OR r.isHidden = :isHidden)")
    Page<Review> findByAdminFilters(@Param("rating") Integer rating, @Param("hasImage") Boolean hasImage, @Param("isReplied") Boolean isReplied, @Param("isHidden") Boolean isHidden, @Param("variantId") Long variantId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false AND r.rating = :rating")
    Page<Review> findByVariantIdAndRatingAndIsHiddenFalse(@Param("variantId") Long variantId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false AND r.images IS NOT EMPTY")
    Page<Review> findByVariantIdWithImagesAndIsHiddenFalse(@Param("variantId") Long variantId, Pageable pageable);
    
    Optional<Review> findByIdAndUserId(Long id, String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false")
    Double getAverageRating(@Param("variantId") Long variantId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false")
    Long getReviewCount(@Param("variantId") Long variantId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.variantId = :variantId AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> countRatingByVariantId(@Param("variantId") Long variantId);

    List<Review> findByUserId(String userId);
}
