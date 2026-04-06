package com.bkeuty.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {
    private String address;
    private WardDto ward;
    private DistrictDto district;
    private ProvinceDto province;
}
