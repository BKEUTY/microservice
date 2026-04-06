package com.bkeuty.user_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String lastname;
    private String firstname;
    private String email;
    private String phoneNumber;
}
