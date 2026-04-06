package com.bkeuty.product.entity;

import com.bkeuty.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true )
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @Builder.Default
    private BigDecimal price = BigDecimal.valueOf(0);
    @Builder.Default
    private Integer stockQuantity = 0;
    private String description;
    private String productImageUrl;
    private String productVariantName;
    @ManyToMany
    @JoinTable(
            name = "variant_option_values",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id")
    )
    private Set<ProductOptionValue> optionValues;
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Integer reviewCount = 0;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
