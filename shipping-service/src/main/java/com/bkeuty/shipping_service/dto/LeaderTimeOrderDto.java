package com.bkeuty.shipping_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaderTimeOrderDto {

    @JsonProperty("from_estimate_date")
    private String fromEstimateTime;
    @JsonProperty("to_estimate_date")
    private String toEstimateTime;
}
