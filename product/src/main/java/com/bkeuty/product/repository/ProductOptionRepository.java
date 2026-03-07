package com.bkeuty.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bkeuty.product.entity.ProductOption;
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption,Integer> {
}
