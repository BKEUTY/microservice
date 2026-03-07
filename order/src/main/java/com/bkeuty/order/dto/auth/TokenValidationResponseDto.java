package com.bkeuty.order.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenValidationResponseDto {
    private String userRole;
    private String userId;
    private String firstName;
    private String lastName;

}
