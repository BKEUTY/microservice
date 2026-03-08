package com.bkeuty.auth_service.controller;

import com.bkeuty.auth_service.dto.*;
import com.bkeuty.auth_service.jwtUtil.AccessTokenValidator;
import com.bkeuty.auth_service.service.AuthService;
import com.bkeuty.auth_service.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto, HttpServletResponse response) {
        try {
            LoginResponseDto loginResponse = authService.loginUser(loginDto);

            Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(loginResponse.getRefreshTokenExpiresIn());
            response.addCookie(refreshTokenCookie);

            loginResponse.setRefreshToken(null); 
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Wrong credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong credentials");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, 
                                        @RequestBody(required = false) RefreshTokenRequestDto refreshTokenDto,
                                        HttpServletResponse response) {
        try {
            String tokenToUse = refreshToken != null ? refreshToken : (refreshTokenDto != null ? refreshTokenDto.getRefreshToken() : null);
            
            if (tokenToUse == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is missing");
            }

            RefreshTokenResponseDto refreshResponse = authService.refreshToken(new RefreshTokenRequestDto(tokenToUse));
            
            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok("Logged out successfully");
    }
}
