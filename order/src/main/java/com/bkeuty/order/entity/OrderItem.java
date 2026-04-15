package com.bkeuty.order.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "product_variant_name")
    private String productVariantName;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "promotion_price", precision = 19, scale = 2)
    private BigDecimal promotionPrice;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @Builder.Default
    private boolean isReviewed = false;
}
