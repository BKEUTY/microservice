package com.bkeuty.order.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class GetShippingOrderStatusRequest {
    Integer orderId;
    OrderCodeDto orderCode;
}
