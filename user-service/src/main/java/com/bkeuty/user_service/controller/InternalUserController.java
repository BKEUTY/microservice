package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/user/internal")
public class InternalUserController {
    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserDetailById(userId));
    }
    
    @PostMapping("/names")
    public ResponseEntity<Map<String, String>> getUserNames(@RequestBody List<String> userIds) {
        return ResponseEntity.ok(userService.getUserNames(userIds));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers(
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        return ResponseEntity.ok(userService.countUsersByDateRange(startDate, endDate));
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDetailResponseDto>> listNewUsers(
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        return ResponseEntity.ok(userService.getNewUsersByDateRange(startDate, endDate));
    }
}
