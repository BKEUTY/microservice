package com.bkeuty.shipping_service.controller;

import com.bkeuty.shipping_service.dto.*;
import com.bkeuty.shipping_service.service.KafkaService;
import com.bkeuty.shipping_service.service.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShippingController.class)
@ActiveProfiles("test")
class ShippingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShippingService shippingService;

    @MockitoBean
    private KafkaService kafkaService;

    @Test
    void getHealthCheck_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/shipping/healthcheck"))
                .andExpect(status().isOk());
    }

    @Test
    void calShippingFee_ShouldReturnFee() throws Exception {
        CalShippingFeeResponseDto response = new CalShippingFeeResponseDto();
        response.setCode(200);
        response.setMessage("Success");

        CalShippingFeeDto requestDto = new CalShippingFeeDto();
        when(shippingService.calShippingFee(any(CalShippingFeeDto.class))).thenReturn(Mono.just(response));

        MvcResult mvcResult = mockMvc.perform(post("/api/shipping/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void calShippingTime_ShouldReturnTime() throws Exception {
        CalShippingTimeResponseDto response = new CalShippingTimeResponseDto();
        response.setCode(200);
        response.setMessage("Success");

        CalShippingTimeDto requestDto = new CalShippingTimeDto();
        when(shippingService.calShippingTime(any(CalShippingTimeDto.class))).thenReturn(Mono.just(response));

        MvcResult mvcResult = mockMvc.perform(post("/api/shipping/leadtime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shippingWebhook_ShouldReturnOk() throws Exception {
        doNothing().when(kafkaService).sendUpdateShippingOrder(any(GhnWebhookDto.class));

        mockMvc.perform(post("/api/shipping/webhook")
                        .param("orderCode", "DH12345")
                        .param("status", "ready_to_pick"))
                .andExpect(status().isOk());
    }
}
