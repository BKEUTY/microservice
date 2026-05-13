package com.bkeuty.order.util;

import com.bkeuty.order.dto.shipping.AddressDto;
import com.bkeuty.order.dto.shipping.DistrictDto;
import com.bkeuty.order.dto.shipping.ProvinceDto;
import com.bkeuty.order.dto.shipping.WardDto;

public class OrderAddressUtils {

    public static String addressDtoToAddress(AddressDto dto) {
        if (dto == null) return null;
        return dto.getAddress() + ", " + dto.getWard().getWardName() + ", " + dto.getDistrict().getDistrictName() + ", " + dto.getProvince().getProvinceName()
                + "|" + dto.getWard().getWardCode().toString()
                + ":" + dto.getDistrict().getDistrictID().toString()
                + ":" + dto.getProvince().getProvinceID().toString();
    }

    public static AddressDto toAddressDto(String address) {
        if (address == null || address.isEmpty()) return null;
        String[] addressArray = address.split("\\|");
        if (addressArray.length != 2) return null;
        
        String nameField = addressArray[0];
        String codeField = addressArray[1];
        String[] nameArray = nameField.split(",\\s*");
        if (nameArray.length < 4) return null;
        
        int nameLength = nameArray.length;
        StringBuilder addressName = new StringBuilder();
        for (int nameIndex = 0; nameIndex < nameLength - 3; nameIndex++) {
            if (nameIndex > 0) addressName.append(", ");
            addressName.append(nameArray[nameIndex]);
        }

        String wardName = nameArray[nameLength - 3];
        String districtName = nameArray[nameLength - 2];
        String provinceName = nameArray[nameLength - 1];
        
        String[] codeArray = codeField.split(":");
        if (codeArray.length != 3) return null;
        
        return AddressDto.builder()
                .address(addressName.toString())
                .ward(new WardDto(Integer.valueOf(codeArray[0]), wardName))
                .district(new DistrictDto(Integer.valueOf(codeArray[1]), districtName))
                .province(new ProvinceDto(Integer.valueOf(codeArray[2]), provinceName))
                .build();
    }
}
