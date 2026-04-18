package com.bkeuty.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.enums.ProductStatus;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Integer>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<ProductVariant> {
        @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
        List<ProductVariant> findAllByProductId(Integer productId);

        @Query("SELECT pv FROM ProductVariant pv WHERE pv.status = com.bkeuty.product.enums.ProductStatus.ACTIVE AND pv.stockQuantity > 0")
        List<ProductVariant> findActiveVariantsWithStock(Pageable pageable);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"product", "product.brand", "product.categories"})
        List<ProductVariant> findAllByIdIn(@Param("productVariantIds") List<Integer> productVariantIds);

        Optional<ProductVariant> findFirstByProductVariantName(String productVariantName);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity, pv.sold = COALESCE(pv.sold, 0) + :quantity WHERE pv.id = :variantId AND pv.stockQuantity >= :quantity")
        int decreaseStockAndIncreaseSold(@Param("variantId") Integer variantId, @Param("quantity") Integer quantity);

//        @Query("""
//                        SELECT new com.bkeuty.product.dto.user.cart.CartProductVariantDto(v.id, v.price, v.productImageUrl, v.productVariantName)
//                        FROM ProductVariant v
//                        WHERE v.id = :productVariantId
//                        """)
//        CartProductVariantDto findDtoByProductVariantId(@Param("productVariantId") Integer productVariantId);


}
