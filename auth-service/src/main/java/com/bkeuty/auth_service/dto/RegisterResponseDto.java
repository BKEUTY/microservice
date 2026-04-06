package com.bkeuty.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressDto address;
}
