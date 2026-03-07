package com.bkeuty.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequestDto {
    private String paymentMethod;
    private String address;
    private List<OrderCartItemDto> orderItems;
}
