package com.bkeuty.product.entity;

import com.bkeuty.product.enums.BrandStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String brandName;
    private String description;
    private String image;
    private String category;
    private BrandStatus brandStatus = BrandStatus.ACTIVE;
}
