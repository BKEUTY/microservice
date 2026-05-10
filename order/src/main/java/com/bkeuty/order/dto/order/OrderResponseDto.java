package com.bkeuty.order.dto.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import com.bkeuty.order.dto.shipping.AddressDto;

import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.enums.PaymentMethod;
import com.bkeuty.order.enums.PaymentStatus;
import java.time.LocalDateTime;
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
    private PaymentMethod paymentMethod;
    private LocalDateTime orderDate;
    private AddressDto address;
    private String buyerName;
    private String buyerPhoneNumber;
    private String buyerNote;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String shippingStatus;
    private String qrCodeLink;
    private BigDecimal voucherDiscountAmount;
    private List<AddToCartResponseDto> items;
}
