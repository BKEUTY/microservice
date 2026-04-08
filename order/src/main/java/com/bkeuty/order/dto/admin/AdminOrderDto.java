package com.bkeuty.order.dto.admin;

import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderDto {
    private Integer id;
    private String userId;
    private String userName;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private String paymentMethod;
    private LocalDate orderDate;
    private String address;
    private String status;
    private List<AddToCartResponseDto> items;
}
