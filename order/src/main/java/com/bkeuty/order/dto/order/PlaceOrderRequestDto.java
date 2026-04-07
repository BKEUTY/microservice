package com.bkeuty.order.dto.order;

import com.bkeuty.order.dto.shipping.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequestDto {
    private String paymentMethod;
    private AddressDto address;
    private List<OrderCartItemDto> orderItems;
    private BigDecimal shippingFee;
}
