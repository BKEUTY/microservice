package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.service.AuthService;
import com.bkeuty.user_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/user/internal")
public class InternalUserController {
    private final UserService userService;
    private final AuthService authService;

    public InternalUserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String userId) {
        if (token != null && !isAuthenticated(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(userService.getUserDetailById(userId));
    }
    
    @PostMapping("/names")
    public ResponseEntity<Map<String, String>> getUserNames(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody List<String> userIds) {
        if (token != null && !isAuthenticated(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(userService.getUserNames(userIds));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        if (token != null && !isAdmin(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(userService.countUsersByDateRange(startDate, endDate));
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDetailResponseDto>> listNewUsers(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        if (token != null && !isAdmin(token)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(userService.getNewUsersByDateRange(startDate, endDate));
    }

    private boolean isAuthenticated(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto val = authService.validateToken(token);
            if (val == null || val.getUserId() == null) return false;
            String role = val.getUserRole();
            return "ADMIN".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        try {
            TokenValidationResponseDto val = authService.validateToken(token);
            return val != null && "ADMIN".equalsIgnoreCase(val.getUserRole());
        } catch (Exception e) {
            return false;
        }
    }
}
