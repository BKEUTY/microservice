package com.bkeuty.order.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateOrderStatusRequestDto {
    private String status;
    private String paymentStatus;
}
