package com.bkeuty.promotion_service.entity;

import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorColumn(name = "coupon_type")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private PromotionStatus status;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer maxDiscount;

}
