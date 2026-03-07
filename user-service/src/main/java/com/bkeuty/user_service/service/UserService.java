package com.bkeuty.user_service.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realmName; // "bkeuty"

    public UserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
    public UserRepresentation getUserProfile() {
        UserResource response = keycloak.realm(realmName).users().get("491e9c7c-9ad1-490c-9a23-24d0849245c6");
        if (response != null) {
            UserRepresentation userRepresentation =response.toRepresentation();
            userRepresentation.setEmail("quangviewirweir@gmail.com");
            System.out.println("User username"+userRepresentation.getUsername());
            return userRepresentation;
        }


        return null;
    }
}
