package com.bkeuty.product.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.bkeuty.product.enums.ProductStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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
    private BigDecimal promotionPrice = BigDecimal.valueOf(0);
    @Builder.Default
    private Integer stockQuantity = 0;
    private String description;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProductImage> productImageUrls;
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
    @Builder.Default
    private Integer sold = 0;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @PrePersist
    public void prePersist() {
        if (this.promotionPrice == null) {
            this.promotionPrice = this.price;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.promotionPrice == null) {
            this.promotionPrice = this.price;
        }
    }
}
