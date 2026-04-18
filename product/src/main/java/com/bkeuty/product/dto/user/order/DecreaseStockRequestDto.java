package com.bkeuty.product.dto.user.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecreaseStockRequestDto {
    private Integer orderId;
    List<OrderItemDto> orderItems;
}
