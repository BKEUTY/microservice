package com.bkeuty.order.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateShippingOrderMessage {
    Integer orderId;
    CreateShippingOrderDto createShippingOrderDto;
}
