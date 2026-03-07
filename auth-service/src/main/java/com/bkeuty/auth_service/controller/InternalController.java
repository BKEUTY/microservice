package com.bkeuty.auth_service.controller;

import com.bkeuty.auth_service.dto.TokenValidationRequestDto;
import com.bkeuty.auth_service.dto.TokenValidationResponseDto;
import com.bkeuty.auth_service.jwtUtil.AccessTokenValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/internal")
public class InternalController {
    private final AccessTokenValidator accessTokenValidator;
    public InternalController(AccessTokenValidator accessTokenValidator) {
        this.accessTokenValidator = accessTokenValidator;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?>  validateAccessToken(@RequestBody TokenValidationRequestDto dto) {
        TokenValidationResponseDto res = accessTokenValidator.validate(dto.getToken());
        if (res != null) {
            return ResponseEntity.ok(res);
        }
        else  {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }

    }

}

