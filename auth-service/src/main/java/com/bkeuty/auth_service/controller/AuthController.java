package com.bkeuty.auth_service.controller;

import com.bkeuty.auth_service.dto.*;
import com.bkeuty.auth_service.jwtUtil.AccessTokenValidator;
import com.bkeuty.auth_service.service.AuthService;
import com.bkeuty.auth_service.service.UserService;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final AccessTokenValidator accessTokenValidator;
    public AuthController(UserService userService, AuthService authService,  AccessTokenValidator accessTokenValidator) {
        this.userService = userService;
        this.authService = authService;
        this.accessTokenValidator = accessTokenValidator;
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto) {
        try {
            LoginResponseDto response = authService.loginUser(loginDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Wrong credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong credentials");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenDto) {
        try {
            RefreshTokenResponseDto response = authService.refreshToken(refreshTokenDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token");
        }
    }
}
