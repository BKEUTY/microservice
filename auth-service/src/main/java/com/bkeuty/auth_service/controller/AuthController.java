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

    public AuthController(UserService userService, AuthService authService, AccessTokenValidator accessTokenValidator) {
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
            String cookieName = "ADMIN".equalsIgnoreCase(loginDto.getClientType()) ? "admin_refreshToken" : "user_refreshToken";
            
            setCookie(response, cookieName, loginResponse.getRefreshToken(), loginResponse.getRefreshTokenExpiresIn());
            loginResponse.setRefreshToken(null); 
            
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            HttpStatus status = "Wrong credentials".equals(e.getMessage()) ? HttpStatus.UNAUTHORIZED : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "admin_refreshToken", required = false) String adminToken,
            @CookieValue(name = "user_refreshToken", required = false) String userToken,
            @RequestHeader(name = "X-Client-Type", required = false) String clientType,
            @RequestBody(required = false) RefreshTokenRequestDto dto,
            HttpServletResponse response) {
        try {
            String token = "ADMIN".equalsIgnoreCase(clientType) ? adminToken : 
                           "USER".equalsIgnoreCase(clientType) ? userToken : 
                           (adminToken != null ? adminToken : userToken);
                           
            token = (token == null && dto != null) ? dto.getRefreshToken() : token;

            if (token == null) return ResponseEntity.badRequest().body("Refresh token is missing");

            RefreshTokenResponseDto refreshResponse = authService.refreshToken(new RefreshTokenRequestDto(token));
            // String cookieName = "ADMIN".equalsIgnoreCase(clientType) ? "admin_refreshToken" : "user_refreshToken";
            // setCookie(response, cookieName, refreshResponse.getRefreshToken(), 1800);
            // refreshResponse.setRefreshToken(null);

            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Invalid refresh token: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "X-Client-Type", required = false) String clientType, HttpServletResponse response) {
        String[] cookies = "ADMIN".equalsIgnoreCase(clientType) ? new String[]{"admin_refreshToken"} :
                           "USER".equalsIgnoreCase(clientType) ? new String[]{"user_refreshToken"} :
                           new String[]{"admin_refreshToken", "user_refreshToken"};

        for (String name : cookies) {
            setCookie(response, name, null, 0);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Chuyển thành true khi deploy production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
