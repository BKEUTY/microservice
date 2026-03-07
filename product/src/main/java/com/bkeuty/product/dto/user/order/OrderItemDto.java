package com.bkeuty.product.dto.user.order;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    Integer productVariantId;
    Integer quantity;
}
