package com.bkeuty.review_service.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInternalResponseDto {
    private String userId;
    private String lastname;
    private String firstname;
    private String email;
}
