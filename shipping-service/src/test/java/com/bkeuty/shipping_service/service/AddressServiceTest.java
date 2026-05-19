package com.bkeuty.shipping_service.service;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.microservicecommunication.GHNCommunication;
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
    void isProvinceValid_ShouldReturnTrue_WhenProvinceMatches() {
        ProvinceDtoGhn province = new ProvinceDtoGhn();
        province.setProvinceID(201);
        province.setProvinceName("Ha Noi");

        GHNProvinceListResponse response = new GHNProvinceListResponse();
        response.setData(List.of(province));

        when(ghnCommunication.getGHNProvinceList()).thenReturn(Mono.just(response));

        assertTrue(addressService.isProvinceValid(province));
    }

    @Test
    void isProvinceValid_ShouldReturnFalse_WhenProvinceNotFound() {
        ProvinceDtoGhn queried = new ProvinceDtoGhn();
        queried.setProvinceID(999);
        queried.setProvinceName("Unknown");

        ProvinceDtoGhn existing = new ProvinceDtoGhn();
        existing.setProvinceID(201);
        existing.setProvinceName("Ha Noi");

        GHNProvinceListResponse response = new GHNProvinceListResponse();
        response.setData(List.of(existing));

        when(ghnCommunication.getGHNProvinceList()).thenReturn(Mono.just(response));

        assertFalse(addressService.isProvinceValid(queried));
    }

    @Test
    void isProvinceValid_ShouldReturnFalse_WhenResponseIsNull() {
        when(ghnCommunication.getGHNProvinceList()).thenReturn(Mono.just(new GHNProvinceListResponse()));

        ProvinceDtoGhn province = new ProvinceDtoGhn();
        province.setProvinceID(201);

        assertFalse(addressService.isProvinceValid(province));
    }

    @Test
    void isDistrictValid_ShouldReturnTrue_WhenDistrictMatches() {
        ProvinceDtoGhn province = new ProvinceDtoGhn();
        province.setProvinceID(201);

        DistrictDtoGhn district = new DistrictDtoGhn();
        district.setDistrictID(1442);
        district.setDistrictName("Dong Da");

        GHNDistrictListResponse response = new GHNDistrictListResponse();
        response.setData(List.of(district));

        when(ghnCommunication.getGHNDistrictList("201")).thenReturn(Mono.just(response));

        assertTrue(addressService.isDistrictValid(province, district));
    }

    @Test
    void isDistrictValid_ShouldReturnFalse_WhenDistrictNotFound() {
        ProvinceDtoGhn province = new ProvinceDtoGhn();
        province.setProvinceID(201);

        DistrictDtoGhn queried = new DistrictDtoGhn();
        queried.setDistrictID(9999);
        queried.setDistrictName("Unknown");

        DistrictDtoGhn existing = new DistrictDtoGhn();
        existing.setDistrictID(1442);
        existing.setDistrictName("Dong Da");

        GHNDistrictListResponse response = new GHNDistrictListResponse();
        response.setData(List.of(existing));

        when(ghnCommunication.getGHNDistrictList("201")).thenReturn(Mono.just(response));

        assertFalse(addressService.isDistrictValid(province, queried));
    }

    @Test
    void isWardValid_ShouldReturnTrue_WhenWardMatches() {
        DistrictDtoGhn district = new DistrictDtoGhn();
        district.setDistrictID(1442);

        WardDtoGhn ward = new WardDtoGhn();
        ward.setWardCode(20101);
        ward.setWardName("O Cho Dua");

        GHNWardListResponse response = new GHNWardListResponse();
        response.setData(List.of(ward));

        when(ghnCommunication.getGHNWardList("1442")).thenReturn(Mono.just(response));

        assertTrue(addressService.isWardValid(district, ward));
    }

    @Test
    void isWardValid_ShouldReturnFalse_WhenWardNotFound() {
        DistrictDtoGhn district = new DistrictDtoGhn();
        district.setDistrictID(1442);

        WardDtoGhn queried = new WardDtoGhn();
        queried.setWardCode(99999);
        queried.setWardName("Unknown");

        WardDtoGhn existing = new WardDtoGhn();
        existing.setWardCode(20101);
        existing.setWardName("O Cho Dua");

        GHNWardListResponse response = new GHNWardListResponse();
        response.setData(List.of(existing));

        when(ghnCommunication.getGHNWardList("1442")).thenReturn(Mono.just(response));

        assertFalse(addressService.isWardValid(district, queried));
    }
}
