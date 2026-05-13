package com.bkeuty.order.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailResponseDto {
    private String userId;
    private String lastname;
    private String firstname;
    private String email;
    private String phoneNumber;
    private String dob;
    private String gender;
    private String userRole;
    private Integer membershipLevel;
    private java.math.BigDecimal totalSpending;
}
