package com.bkeuty.chatbot.dto;

import com.bkeuty.chatbot.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatInteraction {
    private String sessionId;
    private List<ChatMessage> messages;
}
