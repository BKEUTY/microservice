package com.bkeuty.order.dto.payment;

import com.bkeuty.order.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusDto  {
    private Integer orderId;
}
