package com.bkeuty.shipping_service.controller;

import com.bkeuty.shipping_service.dto.GHNDistrictListResponse;
import com.bkeuty.shipping_service.dto.GHNProvinceListResponse;
import com.bkeuty.shipping_service.dto.GHNWardListResponse;
import com.bkeuty.shipping_service.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@ActiveProfiles("test")
class AddressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @Test
    void getGHNProvinceList_ShouldReturnProvinces() throws Exception {
        GHNProvinceListResponse response = new GHNProvinceListResponse();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(List.of());

        when(addressService.getGHNProvinceList()).thenReturn(Mono.just(response));

        MvcResult mvcResult = mockMvc.perform(get("/api/address/province"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void getGHNDistrictList_ShouldReturnDistricts() throws Exception {
        GHNDistrictListResponse response = new GHNDistrictListResponse();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(List.of());

        when(addressService.getGHNDistrictList("1")).thenReturn(Mono.just(response));

        MvcResult mvcResult = mockMvc.perform(get("/api/address/district").param("provinceId", "1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getGHNWardList_ShouldReturnWards() throws Exception {
        GHNWardListResponse response = new GHNWardListResponse();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(List.of());

        when(addressService.getGHNWardList("2")).thenReturn(Mono.just(response));

        MvcResult mvcResult = mockMvc.perform(get("/api/address/ward").param("districtId", "2"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
