package com.bkeuty.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPageResponse {
    private Page<ReviewResponse> reviews;
    private Map<String, Long> ratingCounts;
}
