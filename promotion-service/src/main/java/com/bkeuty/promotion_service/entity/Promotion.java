package com.bkeuty.promotion_service.entity;

import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Data
@DiscriminatorColumn(name = "promotion_type")
@AllArgsConstructor
@NoArgsConstructor
public abstract class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private PromotionStatus status;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer maxDiscount;
    @Column(name = "promotion_type", insertable = false, updatable = false)
    protected String promotionType;

    @ElementCollection
    @CollectionTable(
            name = "promotion_membership_level",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "membership_level")
    @BatchSize(size = 100)
    private java.util.Set<Integer> membershipLevels;
}

