package com.bkeuty.order.dto.admin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.bkeuty.order.dto.cart.AddToCartResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private String paymentStatus;
    private String shippingStatus;
    private List<AddToCartResponseDto> items;
    private List<String> availableStatuses;
}
