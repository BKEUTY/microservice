package com.bkeuty.shipping_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingOrderDto {
    @JsonProperty("order_code")
    private String orderCode;
    @JsonProperty("total_fee")
    private String totalFee;
    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime;
}
