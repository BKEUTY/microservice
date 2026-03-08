package com.bkeuty.product.entity;

import com.bkeuty.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductOptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    private ProductStatus status = ProductStatus.ACTIVE;
    private String optionValueName;
    @ManyToOne
    @JoinColumn(name = "option_id")
    private ProductOption option;
    @ManyToMany(mappedBy = "optionValues")
    private Set<ProductVariant> productVariants;
}
