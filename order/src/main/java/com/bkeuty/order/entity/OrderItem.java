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

import java.math.RoundingMode;

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

    @Column(name = "promotion_price", precision = 19, scale = 2)
    private BigDecimal promotionPrice;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "voucher_discount_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal voucherDiscountAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal productVariantPrice;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @Builder.Default
    private boolean isReviewed = false;
    @Builder.Default
    private Boolean decreasedStockFailed = false;

    public BigDecimal calculateUnitRefundAmount() {
        BigDecimal effectivePrice = promotionPrice != null ? promotionPrice : productVariantPrice;
        
        if (voucherDiscountAmount.compareTo(BigDecimal.ZERO) == 0 || quantity == 0) {
            return effectivePrice;
        }

        BigDecimal unitVoucherDiscount = voucherDiscountAmount.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        return effectivePrice.subtract(unitVoucherDiscount).max(BigDecimal.ZERO);
    }
}
