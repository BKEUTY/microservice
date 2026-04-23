package com.bkeuty.auth_service.service;

import com.bkeuty.auth_service.dto.RegisterRequestDto;
import com.bkeuty.auth_service.dto.RegisterResponseDto;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realmName; // "bkeuty"

    public UserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public RegisterResponseDto registerUser(RegisterRequestDto dto) {
        // Prepare Password
        String address = dto.getAddress().getAddress()+", "+dto.getAddress().getWard().getWardName() + ", "+ dto.getAddress().getDistrict().getDistrictName()+  ", "+ dto.getAddress().getProvince().getProvinceName()
                    + "|" + dto.getAddress().getWard().getWardCode().toString()
                    + ":" + dto.getAddress().getDistrict().getDistrictID().toString()
                    + ":" + dto.getAddress().getProvince().getProvinceID().toString();

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);

        // Map DTO to UserRepresentation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        // Map Custom Attributes (phoneNumber, mainAddress)
        // Keycloak expects a Map<String, List<String>>
        user.setAttributes(Map.of(
                "phoneNumber", List.of(dto.getPhoneNumber()),
                "addresses", List.of(address),
                "userRole", List.of("user"),
                "dob", List.of(dto.getDateOfBirth())
        ));

        // Call the API
        try (Response response = keycloak.realm(realmName).users().create(user)) {
            if (response.getStatus() == 201) {
                System.out.println("User created successfully in realm: " + realmName);
                return toRegisterResponseDto(dto);
            } else if (response.getStatus() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User or Email already exists in the system");
            } else {
                throw new ResponseStatusException(HttpStatus.valueOf(response.getStatus()), "Registration error: " + response.getStatusInfo().getReasonPhrase());
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "System error during registration: " + e.getMessage());
        }
    }
    private RegisterResponseDto toRegisterResponseDto(RegisterRequestDto dto) {
        return RegisterResponseDto.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .address(dto.getAddress())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }
}
