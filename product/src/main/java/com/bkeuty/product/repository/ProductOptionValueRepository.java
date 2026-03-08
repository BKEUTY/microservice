package com.bkeuty.product.repository;

import com.bkeuty.product.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Integer> {
    List<ProductOptionValue> findAllByOptionProductId(Integer productId);
}
