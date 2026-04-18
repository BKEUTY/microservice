package com.bkeuty.order.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetShippingOrderStatusResponseDto {
    String status;
    List<ShippingLogDto> log;
}
