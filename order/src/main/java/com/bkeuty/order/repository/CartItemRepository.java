package com.bkeuty.order.repository;

import com.bkeuty.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(String userId);

    CartItem findByUserIdAndProductVariant(String userId, Integer productVariantId);

    CartItem findByIdAndUserId(Integer id, String userId);

    void deleteByUserId(String userId);
}
