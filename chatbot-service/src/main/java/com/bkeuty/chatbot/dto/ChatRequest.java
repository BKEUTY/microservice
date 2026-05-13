package com.bkeuty.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    @NotBlank(message = "Message content is required")
    private String message;

    private String language;
    private String userId;
    private Integer membershipLevel;
}
