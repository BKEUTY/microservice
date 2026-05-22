package com.bkeuty.chatbot.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDetailDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeImageAsListAndSerializeImageUrl() throws Exception {
        String json = "{\n" +
                "  \"id\": 47,\n" +
                "  \"name\": \"Sữa Rửa Mặt 3CE Limited - 50ml\",\n" +
                "  \"originPrice\": 230000,\n" +
                "  \"promotionPrice\": 172500,\n" +
                "  \"image\": [\"https://example.com/image47.png\"],\n" +
                "  \"stockQuantity\": 104,\n" +
                "  \"sold\": 0,\n" +
                "  \"brand\": \"3CE\",\n" +
                "  \"status\": \"ACTIVE\",\n" +
                "  \"description\": \"Mô tả sữa rửa mặt\"\n" +
                "}";

        ProductDetailDto dto = objectMapper.readValue(json, ProductDetailDto.class);

        // Verify Deserialization
        assertNotNull(dto);
        assertEquals(47, dto.getProductId());
        assertEquals("Sữa Rửa Mặt 3CE Limited - 50ml", dto.getVariantName());
        assertEquals(new BigDecimal("230000"), dto.getOriginPrice());
        assertEquals(new BigDecimal("172500"), dto.getDiscountPrice());
        assertNotNull(dto.getImage());
        assertEquals(1, dto.getImage().size());
        assertEquals("https://example.com/image47.png", dto.getImage().get(0));
        assertEquals("https://example.com/image47.png", dto.getImageUrl());
        assertEquals(104, dto.getStockQuantity());
        assertEquals(0, dto.getSold());
        assertEquals("3CE", dto.getBrand());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("Mô tả sữa rửa mặt", dto.getDescription());

        // Verify Serialization to check imageUrl is included
        String serializedJson = objectMapper.writeValueAsString(dto);
        assertTrue(serializedJson.contains("\"imageUrl\":\"https://example.com/image47.png\""));
        assertTrue(serializedJson.contains("\"image\":[\"https://example.com/image47.png\"]"));
    }

    @Test
    void shouldHandleNullOrEmptyImageGracefully() throws Exception {
        String jsonWithNullImage = "{\n" +
                "  \"id\": 48,\n" +
                "  \"image\": null\n" +
                "}";
        ProductDetailDto dto1 = objectMapper.readValue(jsonWithNullImage, ProductDetailDto.class);
        assertNull(dto1.getImage());
        assertNull(dto1.getImageUrl());

        String jsonWithEmptyImage = "{\n" +
                "  \"id\": 49,\n" +
                "  \"image\": []\n" +
                "}";
        ProductDetailDto dto2 = objectMapper.readValue(jsonWithEmptyImage, ProductDetailDto.class);
        assertNotNull(dto2.getImage());
        assertTrue(dto2.getImage().isEmpty());
        assertNull(dto2.getImageUrl());
    }
}
