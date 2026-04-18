package com.bkeuty.order.dto.order;

import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequestDto {
    private PaymentMethod paymentMethod;
    private AddressDto address;
    private String phoneNumber;
    private String name;
    private String note;
    private List<OrderCartItemDto> orderItems;
    private BigDecimal shippingFee;
}
