package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecreaseVariantDto {
    private Integer productVariantId;
    private Integer quantity;
}
