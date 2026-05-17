package com.bkeuty.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private String userId;
    private String userName;
    private Long variantId;
    private Integer rating;
    private String comment;
    private List<String> images;
    @JsonProperty("isHidden")
    private boolean isHidden;
    @JsonProperty("isReplied")
    private boolean isReplied;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private ReplyResponse reply;
}
