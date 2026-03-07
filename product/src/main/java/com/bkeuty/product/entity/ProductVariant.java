package com.bkeuty.product.entity;

import com.bkeuty.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true )
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private BigDecimal price = BigDecimal.valueOf(0);
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
    private ProductStatus status = ProductStatus.ACTIVE;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;


}
