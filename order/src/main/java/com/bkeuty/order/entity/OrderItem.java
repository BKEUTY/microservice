package com.bkeuty.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer quantity;
    @Column(name = "product_variant_id", nullable = false)
    private Integer productVariantId;
    @Column
    private String productVariantName;
    @Column
    private BigDecimal productVariantPrice;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @Builder.Default
    private boolean isReviewed = false;
    @Builder.Default
    private Boolean decreasedStockFailed = false;
}
