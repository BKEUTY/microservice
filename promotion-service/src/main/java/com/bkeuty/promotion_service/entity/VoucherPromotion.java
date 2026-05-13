package com.bkeuty.promotion_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("VoucherPromotion")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherPromotion extends Promotion {

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_categories", columnDefinition = "jsonb")
    private String applicableCategories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_products", columnDefinition = "jsonb")
    private String applicableProducts;
}
