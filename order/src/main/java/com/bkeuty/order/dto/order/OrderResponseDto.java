package com.bkeuty.order.dto.order;

import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
import com.bkeuty.order.enums.PaymentStatus;
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
    private PaymentMethod paymentMethod;
    private LocalDate orderDate;
    private AddressDto address;
    private String buyerName;
    private String buyerPhoneNumber;
    private String buyerNote;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String shippingStatus;
    private String qrCodeLink;
    private List<AddToCartResponseDto> items;
}
