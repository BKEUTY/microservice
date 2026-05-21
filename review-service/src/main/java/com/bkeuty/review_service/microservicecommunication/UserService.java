package com.bkeuty.review_service.microservicecommunication;

import com.bkeuty.review_service.dto.internal.UserInternalResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final WebClient userWebClient;

    public UserService(@Qualifier("userWebClient") WebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

    public Map<String, String> getUserNames(List<String> userIds) {
        try {
            return userWebClient.post()
                    .uri("/api/user/internal/names")
                    .bodyValue(userIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .block();
        } catch (Exception e) {
            Map<String, String> fallback = new java.util.HashMap<>();
            for (String id : userIds) {
                fallback.put(id, "User " + id);
            }
            return fallback;
        }
    }

    public String getUserName(String userId) {
        try {
            UserInternalResponseDto user = userWebClient.get()
                    .uri("/api/user/internal/" + userId)
                    .retrieve()
                    .bodyToMono(UserInternalResponseDto.class)
                    .block();
            if (user != null) {
                String firstname = user.getFirstname() != null ? user.getFirstname() : "";
                String lastname = user.getLastname() != null ? user.getLastname() : "";
                String fullName = (firstname + " " + lastname).trim();
                return fullName.isEmpty() ? "User " + userId : fullName;
            }
        } catch (Exception e) {
            return "User " + userId;
        }
        return "User " + userId;
    }
}
