package com.bkeuty.promotion_service.controller;

import com.bkeuty.promotion_service.dto.PromotionResponseDto;
import com.bkeuty.promotion_service.enums.DiscountType;
import com.bkeuty.promotion_service.enums.PromotionStatus;
import com.bkeuty.promotion_service.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserPromotionController.class)
class UserPromotionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @Test
    void healthCheck_ShouldReturn200AndOk() throws Exception {
        mockMvc.perform(get("/api/promotion/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void getPromotions_ShouldReturnPageOfPromotions() throws Exception {
        PromotionResponseDto promo = PromotionResponseDto.builder()
                .id(1)
                .title("Khuyen mai dac biet")
                .description("Giam gia 20% cho moi san pham")
                .status(PromotionStatus.STARTING)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(20)
                .promotionType("ProductPromotion")
                .build();

        Page<PromotionResponseDto> page = new PageImpl<>(List.of(promo));
        when(promotionService.findAll(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/promotion")
                        .param("page", "1")
                        .param("size", "10")
                        .param("title", "Khuyen mai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Khuyen mai dac biet")))
                .andExpect(jsonPath("$.content[0].discountValue", is(20)));
    }
}
