package com.bkeuty.order.dto.admin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private Integer orderId;
    private String userId;
    private String userName;
    private BigDecimal total;
    private BigDecimal shippingFee;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private String address;
    private String status;
    private String paymentStatus;
    private String shippingStatus;
    private String estShippingDate;
    private String buyerName;
    private String buyerPhoneNumber;
    private String buyerNote;
    private List<AddToCartResponseDto> items;
    private List<String> availableStatuses;
}
