package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.UpdateUserDto;
import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    Logger logger = Logger.getLogger(UserService.class);
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realmName; // "bkeuty"

    public UserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
    public UserDetailResponseDto getUserProfile(TokenValidationResponseDto tokenValidationResponseDto) {
        UserResource response = keycloak.realm(realmName).users().get(tokenValidationResponseDto.getUserId());
        if (response != null) {
            UserRepresentation userRepresentation =response.toRepresentation();
            userRepresentation.setEmail("quangviewirweir@gmail.com");
            System.out.println("User username"+userRepresentation.getUsername());

            return toUserDetailResponseDto(userRepresentation);
        }
        return null;
    }
    public UpdateUserDto updateUserProfile(UpdateUserDto updateUserDto, TokenValidationResponseDto userInfo) {
        UsersResource usersResource = keycloak.realm(realmName).users();
        UserRepresentation user = new UserRepresentation();

        user.setFirstName(updateUserDto.getFirstname());
        user.setLastName(updateUserDto.getLastname());
        user.setEmail(updateUserDto.getEmail());
        Map<String, List<String>> map = new HashMap<>();
        map.put("phoneNumber", List.of(updateUserDto.getPhoneNumber()));
        map.put("mainAddress", List.of(updateUserDto.getMainAddress()));
        map.put("otherAddress", updateUserDto.getOtherAddresses());
        user.setAttributes(map);
        try {
            usersResource.get(userInfo.getUserId()).update(user);
            return updateUserDto;
        } catch (Exception e){
            logger.error("Exception in updateUserProfile " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in updateUserProfile " + e.getMessage());
        }



    }
    private UserDetailResponseDto toUserDetailResponseDto(UserRepresentation userRepresentation) {
        return  UserDetailResponseDto.builder()
                .email(userRepresentation.getEmail())
                .firstname(userRepresentation.getFirstName())
                .lastname(userRepresentation.getLastName())
                .phoneNumber(userRepresentation.firstAttribute("phoneNumber"))
                .mainAddress(userRepresentation.firstAttribute("mainAddress"))
                .otherAddresses(userRepresentation.getAttributes().get("otherAddress"))
                .build();
    }
}
