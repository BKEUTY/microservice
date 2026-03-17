package com.bkeuty.promotion_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("ProductPromotion")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductPromotion extends Promotion {
    @ElementCollection
    @CollectionTable(
            name = "promotion_category_ids", // Name of the auxiliary table
            joinColumns = @JoinColumn(name = "promotion_id") // Foreign key back to the main table
    )
    @Column(name = "category_id") // Name of the column holding the actual integer
    private Set<Integer> categoryIds = new HashSet<>();;

    @ElementCollection
    @CollectionTable(
            name = "promotion_product_ids",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "product_id")
    private Set<Integer> productIds = new HashSet<>();;

    @ElementCollection
    @CollectionTable(
            name = "promotion_brand_ids",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "brand_id")
    private Set<Integer> brandIds = new HashSet<>();
}
