package com.bkeuty.product.entity;

import com.bkeuty.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductOption {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
    private String optionName;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

