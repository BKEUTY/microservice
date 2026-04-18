package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalShippingFeeDto {
    @JsonProperty("to_ward_code")
    private String toWardCode;
    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    @Builder.Default
    @JsonProperty("weight")
    private Integer weight = 100;
    @Builder.Default
    @JsonProperty("service_type_id")
    private Integer serviceTypeId = 2;

}
