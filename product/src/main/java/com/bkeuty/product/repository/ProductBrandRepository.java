package com.bkeuty.product.repository;

import com.bkeuty.product.entity.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBrandRepository extends JpaRepository<ProductBrand, Integer>, JpaSpecificationExecutor<ProductBrand> {
}
