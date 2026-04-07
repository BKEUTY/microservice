package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaderTimeOrderDto {

    @JsonProperty("from_estimate_date")
    private String fromEstimateTime;
    @JsonProperty("to_estimate_date")
    private String toEstimateTime;
}
