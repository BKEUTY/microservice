package com.bkeuty.user_service.service;

import com.bkeuty.user_service.dto.*;
import com.bkeuty.user_service.dto.auth.TokenValidationResponseDto;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
            System.out.println("User username"+userRepresentation.getUsername());

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

            user.setAttributes(map);
            usersResource.get(userInfo.getUserId()).update(user);
            return updateUserDto;
        } catch (Exception e){
            logger.error("Exception in updateUserProfile " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception in updateUserProfile " + e.getMessage());
        }



    }
    private UserDetailResponseDto toUserDetailResponseDto(UserRepresentation userRepresentation) {
        return  UserDetailResponseDto.builder()
                .userId(userRepresentation.getId())
                .email(userRepresentation.getEmail())
                .firstname(userRepresentation.getFirstName())
                .lastname(userRepresentation.getLastName())
                .phoneNumber(userRepresentation.firstAttribute("phoneNumber"))
                .addresses(userRepresentation.getAttributes().get("addresses")!=null?userRepresentation.getAttributes().get("addresses").stream().map(this::addressToAddressDto).toList():null)
                .dob(userRepresentation.firstAttribute("dob"))
                .userRole(userRepresentation.firstAttribute("userRole"))
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
            logger.error("Exception in updateUserProfile " + e.getMessage());
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
                   System.out.println("Found address");
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
            logger.error("Exception in AddNewAddress " + e.getMessage());
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
            logger.error("Exception in GetAddresses " + e.getMessage());
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
    public List<UserDetailResponseDto>  getListUserDetail(TokenValidationResponseDto tokenValidationResponseDto) {
        return keycloak.realm(realmName).users().list().stream().map(this::toUserDetailResponseDto).toList();
    }
}
