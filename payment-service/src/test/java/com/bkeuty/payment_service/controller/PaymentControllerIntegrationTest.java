package com.bkeuty.payment_service.controller;

import com.bkeuty.payment_service.dto.PaymentWebhookData;
import com.bkeuty.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void sepayWebhook_ShouldReturnCreated_WhenAuthorizedAndSuccessful() throws Exception {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(123);
        webhookData.setContent("Thanh toan don hang 999");
        webhookData.setTransferAmount(new BigDecimal("100000"));
        webhookData.setTransactionDate(LocalDateTime.now());

        when(paymentService.updatePaymentTransaction(any(PaymentWebhookData.class))).thenReturn(true);

        mockMvc.perform(post("/api/payment/webhook")
                        .header("Authorization", "Apikey test-secret-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void sepayWebhook_ShouldReturnUnauthorized_WhenApiKeyIsInvalid() throws Exception {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(123);

        mockMvc.perform(post("/api/payment/webhook")
                        .header("Authorization", "Apikey WRONG_KEY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookData)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sepayWebhook_ShouldReturnBadRequest_WhenAuthHeaderIsMissing() throws Exception {
        PaymentWebhookData webhookData = new PaymentWebhookData();
        webhookData.setId(123);

        mockMvc.perform(post("/api/payment/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookData)))
                .andExpect(status().isBadRequest());
    }
}
