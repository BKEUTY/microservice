package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.service.AuthService;
import com.bkeuty.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AdminController {
    private final UserService userService;
    private final AuthService authService;
    public AdminController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserDetailResponseDto>> getListUserDetail(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestParam(required = false) String role) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null || !tokenValidationResponseDto.getUserRole().equalsIgnoreCase("admin")){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(userService.getListUserDetail(role));
    }
}
