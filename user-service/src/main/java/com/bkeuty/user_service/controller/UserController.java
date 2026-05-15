package com.bkeuty.user_service.controller;

import com.bkeuty.user_service.dto.AddressDto;
import com.bkeuty.user_service.dto.UpdateUserDto;
import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.user_service.service.AuthService;
import com.bkeuty.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<?> getHealthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<UserDetailResponseDto> getUser(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }
        return ResponseEntity.ok(userService.getUserProfile(tokenValidationResponseDto));
    }
    @PutMapping
    public ResponseEntity<UpdateUserDto> updateUser(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,@RequestBody UpdateUserDto updateUserDto) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }
        return ResponseEntity.ok(userService.updateUserProfile(updateUserDto,tokenValidationResponseDto));
    }
    @GetMapping("/address")
    public ResponseEntity<List<AddressDto>> getAddress(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }
        return ResponseEntity.ok(userService.getAddresses(tokenValidationResponseDto));
    }
    @PostMapping("/address")
    public ResponseEntity<Boolean> addAddress(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,@RequestBody AddressDto addNewAddressDto) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(userService.addNewAddress(tokenValidationResponseDto,addNewAddressDto));
    }
    @DeleteMapping("/address")
    public ResponseEntity<Boolean> deleteAddress(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String bearerToken,@RequestBody AddressDto deleteAddressDto) {
        TokenValidationResponseDto tokenValidationResponseDto = authService.validateToken(bearerToken);
        if(tokenValidationResponseDto == null || tokenValidationResponseDto.getUserRole() == null
                || !"user".equals(tokenValidationResponseDto.getUserRole())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You do not have permission to perform this action");
        }
        return ResponseEntity.ok(userService.deleteAddress(deleteAddressDto,tokenValidationResponseDto));
    }
}
