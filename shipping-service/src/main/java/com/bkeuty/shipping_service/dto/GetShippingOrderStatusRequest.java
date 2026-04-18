package com.bkeuty.shipping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class GetShippingOrderStatusRequest {
    Integer orderId;
    String currentStatus;
    OrderCodeDto orderCode;
}
