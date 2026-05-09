package com.bkeuty.chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.bkeuty.chatbot.dto.ProductDetailDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private List<ProductDetailDto> recommendedProduct;
}
