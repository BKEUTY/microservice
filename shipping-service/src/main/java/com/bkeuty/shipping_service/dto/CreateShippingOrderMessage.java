package com.bkeuty.shipping_service.dto;

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
