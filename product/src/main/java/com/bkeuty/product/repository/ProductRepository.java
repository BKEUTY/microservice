package com.bkeuty.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bkeuty.product.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {



}
