package com.bkeuty.auth_service.service;

import com.bkeuty.auth_service.dto.RegisterRequestDto;
import com.bkeuty.auth_service.dto.RegisterResponseDto;
import com.bkeuty.auth_service.dto.AddressDto;
import com.bkeuty.auth_service.dto.DistrictDto;
import com.bkeuty.auth_service.dto.ProvinceDto;
import com.bkeuty.auth_service.dto.WardDto;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @InjectMocks
    private UserService userService;

    private RegisterRequestDto requestDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "realmName", "bkeuty");

        requestDto = new RegisterRequestDto();
        requestDto.setUsername("newuser");
        requestDto.setEmail("test@gmail.com");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setPassword("pass123");
        requestDto.setPhoneNumber("0909090909");
        requestDto.setDateOfBirth("2000-01-01");

        AddressDto address = new AddressDto();
        address.setAddress("123 Main St");
        address.setProvince(new ProvinceDto(201, "Ha Noi"));
        address.setDistrict(new DistrictDto(1442, "Dong Da"));
        address.setWard(new WardDto(1, "O Cho Dua"));
        requestDto.setAddress(address);
    }

    @Test
    void registerUser_ShouldReturnDto_WhenRegistrationIsSuccessful() {
        Response mockResponse = Response.status(201).build();

        when(keycloak.realm("bkeuty")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        RegisterResponseDto responseDto = userService.registerUser(requestDto);

        assertNotNull(responseDto);
        assertEquals("test@gmail.com", responseDto.getEmail());
        assertEquals("John", responseDto.getFirstName());
        verify(usersResource, times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void registerUser_ShouldThrowConflict_WhenUserAlreadyExists() {
        Response mockResponse = Response.status(409).build();

        when(keycloak.realm("bkeuty")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.registerUser(requestDto);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
    }

    @Test
    void registerUser_ShouldThrowException_WhenApiFails() {
        when(keycloak.realm("bkeuty")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Network Error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.registerUser(requestDto);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Network Error"));
    }
}
