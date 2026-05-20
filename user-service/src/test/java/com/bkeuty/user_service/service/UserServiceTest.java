package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.AddressDto;
import com.bkeuty.user_service.dto.DistrictDto;
import com.bkeuty.user_service.dto.ProvinceDto;
import com.bkeuty.user_service.dto.UpdateUserDto;
import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.WardDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Mock
    private UserResource userResource;

    @InjectMocks
    private UserService userService;

    private static final String REALM = "bkeuty";
    private static final String USER_ID = "user-uuid-123";

    private TokenValidationResponseDto tokenInfo;
    private UserRepresentation mockUserRepresentation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "realmName", REALM);

        tokenInfo = new TokenValidationResponseDto();
        tokenInfo.setUserId(USER_ID);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phoneNumber", List.of("0909090909"));
        attributes.put("dob", List.of("2000-01-01"));
        attributes.put("membershipLevel", List.of("1"));
        attributes.put("totalSpending", List.of("500000"));
        attributes.put("userRole", List.of("user"));
        attributes.put("addresses", List.of("123 Main St, O Cho Dua, Dong Da, Ha Noi|1442:201:1"));

        mockUserRepresentation = new UserRepresentation();
        mockUserRepresentation.setId(USER_ID);
        mockUserRepresentation.setEmail("test@gmail.com");
        mockUserRepresentation.setFirstName("John");
        mockUserRepresentation.setLastName("Doe");
        mockUserRepresentation.setAttributes(attributes);

        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void getUserProfile_ShouldReturnDto_WhenUserExists() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);

        UserDetailResponseDto result = userService.getUserProfile(tokenInfo);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("John", result.getFirstname());
        assertEquals(1, result.getMembershipLevel());
        assertEquals(new BigDecimal("500000"), result.getTotalSpending());

        verify(usersResource, times(1)).get(USER_ID);
    }

    @Test
    void getUserDetailById_ShouldReturnDto_WhenUserExists() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);

        UserDetailResponseDto result = userService.getUserDetailById(USER_ID);

        assertNotNull(result);
        assertEquals("Doe", result.getLastname());
        verify(usersResource, times(1)).get(USER_ID);
    }

    @Test
    void updateUserProfile_ShouldUpdateAndReturnDto_WhenSuccess() {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstname("Jane");
        updateDto.setPhoneNumber("0911111111");

        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        UpdateUserDto result = userService.updateUserProfile(updateDto, tokenInfo);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstname());
        verify(userResource, times(1)).update(any(UserRepresentation.class));
    }

    @Test
    void addNewAddress_ShouldReturnTrue_WhenSuccess() {
        AddressDto newAddress = new AddressDto();
        newAddress.setAddress("456 Other St");
        newAddress.setWard(new WardDto(1442, "Bach Khoa"));
        newAddress.setDistrict(new DistrictDto(201, "Hai Ba Trung"));
        newAddress.setProvince(new ProvinceDto(1, "Ha Noi"));

        List<String> mutableAddresses = new ArrayList<>(List.of("123 Main St, O Cho Dua, Dong Da, Ha Noi|1442:201:1"));
        mockUserRepresentation.getAttributes().put("addresses", mutableAddresses);

        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        Boolean result = userService.addNewAddress(tokenInfo, newAddress);

        assertTrue(result);
        verify(userResource, times(1)).update(any(UserRepresentation.class));
    }

    @Test
    void updateMembershipLevel_ShouldUpdateAttributes_WhenSuccess() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        assertDoesNotThrow(() -> userService.updateMembershipLevel(USER_ID, 2, new BigDecimal("1500000")));

        verify(userResource, times(1)).update(any(UserRepresentation.class));
    }

    @Test
    void updateMembershipLevel_ShouldThrowException_WhenKeycloakFails() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new RuntimeException("Keycloak Error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateMembershipLevel(USER_ID, 2, BigDecimal.TEN));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void getUserNames_ShouldReturnMap_WithFullNames() {
        UserRepresentation user2 = new UserRepresentation();
        user2.setId("user-uuid-456");
        user2.setFirstName("Alice");
        user2.setLastName("Wonder");

        UserResource userResource2 = mock(UserResource.class);
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(usersResource.get("user-uuid-456")).thenReturn(userResource2);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        when(userResource2.toRepresentation()).thenReturn(user2);

        Map<String, String> result = userService.getUserNames(List.of(USER_ID, "user-uuid-456"));

        assertNotNull(result);
        assertEquals("John Doe", result.get(USER_ID));
        assertEquals("Alice Wonder", result.get("user-uuid-456"));
    }

    @Test
    void getAddresses_ShouldReturnList_WhenAddressesExist() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);

        List<AddressDto> addresses = userService.getAddresses(tokenInfo);

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
    }

    @Test
    void updateUserWallet_ShouldAddAmountToWallet_WhenSuccess() {
        mockUserRepresentation.getAttributes().put("wallet", new java.util.ArrayList<>(List.of("100000")));

        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        assertDoesNotThrow(() -> userService.updateUserWallet(USER_ID, new BigDecimal("50000")));

        verify(userResource, times(1)).update(any(UserRepresentation.class));
        
        String finalWalletValue = mockUserRepresentation.getAttributes().get("wallet").get(0);
        assertEquals(0, new BigDecimal("150000").compareTo(new BigDecimal(finalWalletValue)));
    }

    @Test
    void updateUserWallet_ShouldInitializeWalletAndAddAmount_WhenWalletAttributeMissing() {
        // Remove the wallet attribute entirely to simulate a new user
        mockUserRepresentation.getAttributes().remove("wallet");

        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(mockUserRepresentation);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        assertDoesNotThrow(() -> userService.updateUserWallet(USER_ID, new BigDecimal("75000")));

        verify(userResource, times(1)).update(any(UserRepresentation.class));
        
        String finalWalletValue = mockUserRepresentation.getAttributes().get("wallet").get(0);
        assertEquals(0, new BigDecimal("75000").compareTo(new BigDecimal(finalWalletValue)));
    }

    @Test
    void updateUserWallet_ShouldThrowException_WhenKeycloakFails() {
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new RuntimeException("Keycloak Error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateUserWallet(USER_ID, new BigDecimal("50000")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Failed to update wallet"));
    }
}
