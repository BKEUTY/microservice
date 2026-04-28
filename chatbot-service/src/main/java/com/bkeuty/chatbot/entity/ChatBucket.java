package com.bkeuty.chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_buckets")
public class ChatBucket {
    @Id
    private String id;
    private String sessionId;
    private Integer bucketIndex;
    private Integer messageCount;
    private List<ChatMessage> messages;
}
