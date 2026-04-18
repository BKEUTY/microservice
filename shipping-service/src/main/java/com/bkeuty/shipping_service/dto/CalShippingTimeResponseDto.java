package com.bkeuty.shipping_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalShippingTimeResponseDto {
    private Integer code;
    private String message;
    private ShippingTimeDto data;
}
