package com.bkeuty.product.dto.user.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPreviewDto {
    private Long id;
    private String userName;
    private Integer rating;
    private String comment;
    private List<String> images;
    private LocalDateTime createdAt;
}
