package com.bkeuty.product.repository;

import com.bkeuty.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
    List<ProductOption> findAllByProductId(Integer productId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT po.optionName FROM ProductOption po")
    List<String> findDistinctOptionNames();
}
