package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalShippingTimeDto {
    @JsonProperty("to_ward_code")
    private String toWardCode;
    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    @JsonProperty("service_type_id")
    private Integer serviceTypeId = 2;
}
