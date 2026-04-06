package com.bkeuty.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyResponse {
    private Long id;
    private String adminId;
    private String comment;
    private LocalDateTime repliedAt;
    private LocalDateTime updatedAt;
}
