package com.bkeuty.order.dto.order;

import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.shipping.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private String orderId;
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
