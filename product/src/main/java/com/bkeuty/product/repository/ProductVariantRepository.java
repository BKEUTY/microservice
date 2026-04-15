package com.bkeuty.product.repository;

import com.bkeuty.product.dto.user.cart.CartProductVariantDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Integer> {
        @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
        List<ProductVariant> findAllByProductId(Integer productId);

        @Query("SELECT DISTINCT pv FROM ProductVariant pv " +
                        "LEFT JOIN pv.product p " +
                        "LEFT JOIN p.categories c " +
                        "WHERE (:pattern IS NULL OR LOWER(pv.productVariantName) LIKE :pattern) AND " +
                        "(:categoryId IS NULL OR c.id = :categoryId) AND " +
                        "(:status IS NULL OR pv.status = :status)"
        )
        Page<ProductVariant> findWithFilters(
                        @Param("pattern") String pattern,
                        @Param("categoryId") Integer categoryId,
                        @Param("status") ProductStatus status,
                        Pageable pageable);

        @Query("SELECT pv FROM ProductVariant pv WHERE pv.status = com.bkeuty.product.enums.ProductStatus.ACTIVE AND pv.stockQuantity > 0")
        List<ProductVariant> findActiveVariantsWithStock(Pageable pageable);

        @Query("SELECT v FROM ProductVariant v where v.id IN :productVariantIds")
        List<ProductVariant> findDtoByProductVariantIdIn(@Param("productVariantIds") List<Integer> productVariantIds);

        Optional<ProductVariant> findFirstByProductVariantName(String productVariantName);

        @Modifying
        @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity, pv.sold = pv.sold + :quantity WHERE pv.id = :variantId AND pv.stockQuantity >= :quantity")
        int decreaseStockAndIncreaseSold(@Param("variantId") Integer variantId, @Param("quantity") Integer quantity);

//        @Query("""
//                        SELECT new com.bkeuty.product.dto.user.cart.CartProductVariantDto(v.id, v.price, v.productImageUrl, v.productVariantName)
//                        FROM ProductVariant v
//                        WHERE v.id = :productVariantId
//                        """)
//        CartProductVariantDto findDtoByProductVariantId(@Param("productVariantId") Integer productVariantId);


}
