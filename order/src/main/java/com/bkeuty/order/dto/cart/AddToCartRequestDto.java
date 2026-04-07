package com.bkeuty.order.dto.cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequestDto {
    private Integer productVariantId;
    private Integer quantity;
    private Boolean buyNow = false;
}
