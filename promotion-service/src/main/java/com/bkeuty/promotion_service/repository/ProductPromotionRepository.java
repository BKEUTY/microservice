package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {
}
