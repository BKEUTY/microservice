package com.bkeuty.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long variantId;
    private Integer rating;
    private String comment;
    private List<String> images;
}
