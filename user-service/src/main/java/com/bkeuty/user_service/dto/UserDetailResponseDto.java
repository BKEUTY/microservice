package com.bkeuty.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailResponseDto {
    private String userId;
    private String lastname;
    private String firstname;
    private String email;
    private List<AddressDto> addresses;
    private String phoneNumber;
    private String dob;
    private String userRole;

}
