package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private GHNCommunication ghnCommunication;

    @InjectMocks
    private AddressService addressService;

    @Test
    void isProvinceValid_ShouldReturnTrue_WhenProvinceExists() {
        ProvinceDtoGhn targetProvince = new ProvinceDtoGhn();
        targetProvince.setProvinceID(201);
        targetProvince.setProvinceName("Hà Nội");

        GHNProvinceListResponse mockResponse = new GHNProvinceListResponse();
        mockResponse.setData(List.of(targetProvince));

        when(ghnCommunication.getGHNProvinceList()).thenReturn(Mono.just(mockResponse));

        boolean isValid = addressService.isProvinceValid(targetProvince);

        assertTrue(isValid);
        verify(ghnCommunication, times(1)).getGHNProvinceList();
    }

    @Test
    void isProvinceValid_ShouldReturnFalse_WhenProvinceDoesNotExist() {
        ProvinceDtoGhn targetProvince = new ProvinceDtoGhn();
        targetProvince.setProvinceID(999);
        targetProvince.setProvinceName("Tỉnh Lạ");

        ProvinceDtoGhn existingProvince = new ProvinceDtoGhn();
        existingProvince.setProvinceID(201);
        existingProvince.setProvinceName("Hà Nội");

        GHNProvinceListResponse mockResponse = new GHNProvinceListResponse();
        mockResponse.setData(List.of(existingProvince));

        when(ghnCommunication.getGHNProvinceList()).thenReturn(Mono.just(mockResponse));

        boolean isValid = addressService.isProvinceValid(targetProvince);

        assertFalse(isValid);
    }

    @Test
    void isDistrictValid_ShouldReturnTrue_WhenDistrictExists() {
        ProvinceDtoGhn province = new ProvinceDtoGhn();
        province.setProvinceID(201);

        DistrictDtoGhn targetDistrict = new DistrictDtoGhn();
        targetDistrict.setDistrictID(1442);
        targetDistrict.setDistrictName("Quận Đống Đa");

        GHNDistrictListResponse mockResponse = new GHNDistrictListResponse();
        mockResponse.setData(List.of(targetDistrict));

        when(ghnCommunication.getGHNDistrictList("201")).thenReturn(Mono.just(mockResponse));

        boolean isValid = addressService.isDistrictValid(province, targetDistrict);

        assertTrue(isValid);
    }

    @Test
    void isWardValid_ShouldReturnTrue_WhenWardExists() {
        DistrictDtoGhn district = new DistrictDtoGhn();
        district.setDistrictID(1442);

        WardDtoGhn targetWard = new WardDtoGhn();
        targetWard.setWardCode(1);
        targetWard.setWardName("Phường Ô Chợ Dừa");

        GHNWardListResponse mockResponse = new GHNWardListResponse();
        mockResponse.setData(List.of(targetWard));

        when(ghnCommunication.getGHNWardList("1442")).thenReturn(Mono.just(mockResponse));

        boolean isValid = addressService.isWardValid(district, targetWard);

        assertTrue(isValid);
    }
}
