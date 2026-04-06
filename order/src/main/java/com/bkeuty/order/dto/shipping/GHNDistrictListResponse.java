package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHNDistrictListResponse {
    private Integer code;
    private String message;
    private List<DistrictDto> data;
}
