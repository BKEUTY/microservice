package com.bkeuty.order.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDto {
    private String userId;
    private String email;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String dob;
    private String gender;
    private String userRole;
    private String imageUrl;
}
