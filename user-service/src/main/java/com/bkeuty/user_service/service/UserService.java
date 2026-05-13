package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.AddressDto;
import com.bkeuty.user_service.dto.DistrictDto;
import com.bkeuty.user_service.dto.ProvinceDto;
import com.bkeuty.user_service.dto.UpdateUserDto;
import com.bkeuty.user_service.dto.UserDetailResponseDto;
import com.bkeuty.user_service.dto.WardDto;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {
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
            log.info("User username: {}", userRepresentation.getUsername());

            return toUserDetailResponseDto(userRepresentation);
        }
        return null;
    }
    public UpdateUserDto updateUserProfile(UpdateUserDto updateUserDto, TokenValidationResponseDto userInfo) {
        UsersResource usersResource = keycloak.realm(realmName).users();

        try {
            UserResource userResource = usersResource.get(userInfo.getUserId());
            UserRepresentation user = userResource.toRepresentation();
            if(updateUserDto.getFirstname()!=null){
                user.setFirstName(updateUserDto.getFirstname());
            }
            if(updateUserDto.getLastname()!=null){
                user.setLastName(updateUserDto.getLastname());
            }
            if(updateUserDto.getEmail()!=null){
                user.setEmail(updateUserDto.getEmail());
            }

            Map<String, List<String>> map = user.getAttributes();
            if(updateUserDto.getPhoneNumber()!=null){
                map.put("phoneNumber", List.of(updateUserDto.getPhoneNumber()));
            }
            if(updateUserDto.getDob()!=null){
                map.put("dob", List.of(updateUserDto.getDob()));
            }
            if(updateUserDto.getGender()!=null){
                map.put("gender", List.of(updateUserDto.getGender()));
            }

            user.setAttributes(map);
            usersResource.get(userInfo.getUserId()).update(user);
            return updateUserDto;
        } catch (Exception e){
            log.error("Exception: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in updateUserProfile " + e.getMessage());
        }



    }
    private UserDetailResponseDto toUserDetailResponseDto(UserRepresentation userRepresentation) {
        Integer membershipLevel = 0;
        try {
            String levelStr = userRepresentation.firstAttribute("membershipLevel");
            if (levelStr != null) membershipLevel = Integer.parseInt(levelStr);
        } catch (NumberFormatException ignored) {}

        java.math.BigDecimal totalSpending = java.math.BigDecimal.ZERO;
        try {
            String spendingStr = userRepresentation.firstAttribute("totalSpending");
            if (spendingStr != null) totalSpending = new java.math.BigDecimal(spendingStr);
        } catch (Exception ignored) {}

        return UserDetailResponseDto.builder()
                .userId(userRepresentation.getId())
                .email(userRepresentation.getEmail())
                .firstname(userRepresentation.getFirstName())
                .lastname(userRepresentation.getLastName())
                .phoneNumber(userRepresentation.firstAttribute("phoneNumber"))
                .addresses(userRepresentation.getAttributes().get("addresses")!=null?userRepresentation.getAttributes().get("addresses").stream().map(this::addressToAddressDto).toList():null)
                .dob(userRepresentation.firstAttribute("dob"))
                .gender(userRepresentation.firstAttribute("gender"))
                .userRole(userRepresentation.firstAttribute("userRole"))
                .membershipLevel(membershipLevel)
                .totalSpending(totalSpending)
                .build();
    }

    public Boolean addNewAddress(TokenValidationResponseDto tokenValidationResponseDto, AddressDto addNewAddressDto) {
        String address = addNewAddressDto.getAddress()+", "+addNewAddressDto.getWard().getWardName() + ", "+ addNewAddressDto.getDistrict().getDistrictName()+  ", "+ addNewAddressDto.getProvince().getProvinceName()
                + "|" + addNewAddressDto.getWard().getWardCode().toString()
                + ":" + addNewAddressDto.getDistrict().getDistrictID().toString()
                + ":" + addNewAddressDto.getProvince().getProvinceID().toString();
        UsersResource usersResource = keycloak.realm(realmName).users();

        try {
            UserResource updateUser = usersResource.get(tokenValidationResponseDto.getUserId());
            List<String> listAddress = updateUser.toRepresentation().getAttributes().get("addresses");
            UserRepresentation user = updateUser.toRepresentation();
            Map<String, List<String>> map = user.getAttributes();
            if(listAddress==null){
                listAddress = new ArrayList<>();
            }
            listAddress.add(address);
            map.put("addresses", listAddress);
            user.setAttributes(map);
            updateUser.update(user);
            return true;
        } catch (Exception e){
            log.error("Exception in updateUserProfile " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in add new address " + e.getMessage());
        }
    }
    public Boolean deleteAddress(AddressDto address,  TokenValidationResponseDto tokenValidationResponseDto) {
        try {



            UsersResource usersResource = keycloak.realm(realmName).users();
            UserResource updateUser = usersResource.get(tokenValidationResponseDto.getUserId());
            UserRepresentation user = updateUser.toRepresentation();
            Map<String, List<String>> map = user.getAttributes();
            List<String> listAddress = updateUser.toRepresentation().getAttributes().get("addresses");
           boolean deleted  = false;
           if(listAddress.size()==1){
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Should have at least one address");
           }
            for(String key : listAddress) {
                AddressDto addressDto = addressToAddressDto(key);
               if(addressDto.equals(address)) {
                   log.info("Found address to delete for user: {}", tokenValidationResponseDto.getUserId());
                   listAddress.remove(key);
                   deleted = true;
                   break;
               }
           }
            map.put("addresses", listAddress);
            user.setAttributes(map);
            updateUser.update(user);
            return deleted;
        } catch (Exception e){
            log.error("Exception in deleteAddress: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in add new address " + e.getMessage());
        }
    }
    public List<AddressDto> getAddresses(TokenValidationResponseDto tokenValidationResponseDto) {
        try{

            List<String> listAddress = keycloak.realm(realmName).users().get(tokenValidationResponseDto.getUserId()).toRepresentation().getAttributes().get("addresses");
            if(listAddress==null){
                return new ArrayList<>();
            }
            return listAddress.stream().map(this::addressToAddressDto).toList();
        }catch (Exception e){
            log.error("Exception in getAddresses: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in GetAddresses " + e.getMessage());
        }
    }
    private AddressDto addressToAddressDto(String address) {

        String[] addressArray = address.split("\\|");
        if(addressArray.length!=2){
            return null;
        }
        String nameField = addressArray[0];
        String codeField = addressArray[1];
        String[] nameArray = nameField.split(",\\s*");
        if(nameArray.length< 4){
            return null;
        }
        int nameLength = nameArray.length;

        StringBuilder addressName = new StringBuilder();
        for (int nameIndex = 0; nameIndex < nameLength - 3; nameIndex++) {
            if (nameIndex > 0) {
                addressName.append(", ");
            }
            addressName.append(nameArray[nameIndex]);
        }

        String wardName  = nameArray[nameLength-3];
        String districtName = nameArray[nameLength-2];
        String provinceName = nameArray[nameLength-1];
        String[] codeArray = codeField.split(":");
        if(codeArray.length!=3){
            return null;
        }
        String wardCode = codeArray[0];
        String districtCode = codeArray[1];
        String provinceCode = codeArray[2];
        return AddressDto.builder()
                .address(addressName.toString())
                .ward(new WardDto(Integer.valueOf(wardCode), wardName))
                .district(new DistrictDto(Integer.valueOf(districtCode), districtName))
                .province(new ProvinceDto(Integer.valueOf(provinceCode), provinceName))
                .build();
    }
    public UserDetailResponseDto getUserDetailById(String userId) {
        UserResource response = keycloak.realm(realmName).users().get(userId);
        if (response != null) {
            UserRepresentation userRepresentation = response.toRepresentation();
            return toUserDetailResponseDto(userRepresentation);
        }
        return null;
    }

    public void updateMembershipLevel(String userId, Integer level, java.math.BigDecimal totalSpending) {
        try {
            UsersResource usersResource = keycloak.realm(realmName).users();
            UserResource userResource = usersResource.get(userId);
            UserRepresentation user = userResource.toRepresentation();
            Map<String, List<String>> attrs = user.getAttributes();
            if (attrs == null) attrs = new HashMap<>();
            attrs.put("membershipLevel", List.of(String.valueOf(level)));
            if (totalSpending != null) {
                attrs.put("totalSpending", List.of(totalSpending.toPlainString()));
            }
            user.setAttributes(attrs);
            userResource.update(user);
        } catch (Exception e) {
            log.error("Failed to update membershipLevel for user " + userId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update membership level");
        }
    }

    public Map<String, String> getUserNames(List<String> userIds) {
        Map<String, String> result = new HashMap<>();
        for (String id : userIds) {
            try {
                UserRepresentation user = keycloak.realm(realmName).users().get(id).toRepresentation();
                String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                String lastName = user.getLastName() != null ? user.getLastName() : "";
                String fullName = (firstName + " " + lastName).trim();
                result.put(id, fullName.isEmpty() ? "User " + id : fullName);
            } catch (Exception e) {
                result.put(id, "User " + id);
            }
        }
        return result;
    }

    public List<UserDetailResponseDto> getListUserDetail(String role) {
        return keycloak.realm(realmName).users().list().stream()
                .sorted(Comparator.comparing(UserRepresentation::getCreatedTimestamp))
                .map(this::toUserDetailResponseDto)
                .filter(u -> role == null || (u.getUserRole() != null && u.getUserRole().equalsIgnoreCase(role)))
                .toList();
    }

    public List<UserDetailResponseDto> getNewUsersByDateRange(Long startDate, Long endDate) {
        return getFilteredUserRepresentations(startDate, endDate).stream()
                .map(this::toUserDetailResponseDto)
                .toList();
    }

    public long countUsersByDateRange(Long startDate, Long endDate) {
        UsersResource usersResource = keycloak.realm(realmName).users();
        int first = 0;
        int max = 100;
        long count = 0;

        while (true) {
            List<UserRepresentation> usersBatch = usersResource.list(first, max);
            if (usersBatch.isEmpty()) {
                break;
            }

            count += usersBatch.stream()
                    .filter(u -> isUserInDateRangeAndRole(u, startDate, endDate, "USER"))
                    .count();

            if (usersBatch.size() < max) {
                break;
            }
            first += usersBatch.size();
        }
        return count;
    }

    private boolean isUserInDateRangeAndRole(UserRepresentation u, Long startDate, Long endDate, String role) {
        boolean inDateRange = u.getCreatedTimestamp() != null &&
                (startDate == null || u.getCreatedTimestamp() >= startDate) &&
                (endDate == null || u.getCreatedTimestamp() <= endDate);
        
        if (!inDateRange) return false;

        if (u.getAttributes() == null || u.getAttributes().get("userRole") == null || u.getAttributes().get("userRole").isEmpty()) {
            return false;
        }
        
        return u.getAttributes().get("userRole").get(0).equalsIgnoreCase(role);
    }

    private List<UserRepresentation> getFilteredUserRepresentations(Long startDate, Long endDate) {
        UsersResource usersResource = keycloak.realm(realmName).users();
        int first = 0;
        int max = 100;
        List<UserRepresentation> result = new ArrayList<>();

        while (true) {
            List<UserRepresentation> usersBatch = usersResource.list(first, max);
            if (usersBatch.isEmpty()) {
                break;
            }

            usersBatch.stream()
                    .filter(u -> isUserInDateRangeAndRole(u, startDate, endDate, "USER"))
                    .forEach(result::add);

            if (usersBatch.size() < max) {
                break;
            }
            first += usersBatch.size();
        }
        return result;
    }
}
