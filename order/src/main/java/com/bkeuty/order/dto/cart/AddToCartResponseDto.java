package com.bkeuty.order.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartResponseDto {
    private Integer productVariantId;
    private Integer cartId;
    private String productVariantName;
    private String productVariantImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal promotionPrice;
    private BigDecimal voucherDiscountAmount;
}
