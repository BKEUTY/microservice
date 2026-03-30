package com.bkeuty.product.entity;

import com.bkeuty.product.enums.BrandStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String brandName;
    private String description;
    private String image;
    private String category;
    @Builder.Default
    private BrandStatus brandStatus = BrandStatus.ACTIVE;
}
