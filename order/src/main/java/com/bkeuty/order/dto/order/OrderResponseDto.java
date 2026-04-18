package com.bkeuty.order.dto.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.shipping.AddressDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDto {
    private String orderId;
    private String userName;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private String estShippingDate;
    private String paymentMethod;
    private LocalDate orderDate;
    private AddressDto address;
    private String status;
    private String qrCodeLink;
    private List<AddToCartResponseDto> items;
}
