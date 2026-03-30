package com.bkeuty.product.entity;

import com.bkeuty.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductOptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
    private String optionValueName;
    @ManyToOne
    @JoinColumn(name = "option_id")
    private ProductOption option;
    @ManyToMany(mappedBy = "optionValues")
    private Set<ProductVariant> productVariants;
}
