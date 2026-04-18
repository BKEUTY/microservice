package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecreaseStockRequestDto {
    private Integer orderId;
    private List<OrderItemDto> orderItems;
}
