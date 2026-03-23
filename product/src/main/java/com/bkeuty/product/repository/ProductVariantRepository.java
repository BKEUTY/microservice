package com.bkeuty.product.repository;

import com.bkeuty.product.dto.user.cart.ProductVariantDto;
import com.bkeuty.product.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Integer> {
        @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
        List<ProductVariant> findAllByProductId(Integer productId);

        @Query("SELECT DISTINCT pv FROM ProductVariant pv " +
                        "LEFT JOIN pv.product p " +
                        "LEFT JOIN p.categories c " +
                        "WHERE (:pattern IS NULL OR LOWER(pv.productVariantName) LIKE :pattern) AND " +
                        "(:categoryId IS NULL OR c.id = :categoryId)" +
                        "AND pv.status = com.bkeuty.product.enums.ProductStatus.ACTIVE"
        )
        Page<ProductVariant> findWithFilters(
                        @Param("pattern") String pattern,
                        @Param("categoryId") Integer categoryId,
                        Pageable pageable);

        @Query("SELECT new com.bkeuty.product.dto.user.cart.ProductVariantDto(v.id,v.price, v.productImageUrl, v.productVariantName) FROM ProductVariant v where v.id IN :productVariantIds")
        List<ProductVariantDto> findDtoByProductVariantIdIn(@Param("productVariantIds") List<Integer> productVariantIds);

        @Query("""
                        SELECT new com.bkeuty.product.dto.user.cart.ProductVariantDto(v.id, v.price, v.productImageUrl, v.productVariantName)
                        FROM ProductVariant v
                        WHERE v.id = :productVariantId
                        """)
        ProductVariantDto findDtoByProductVariantId(@Param("productVariantId") Integer productVariantId);


}
