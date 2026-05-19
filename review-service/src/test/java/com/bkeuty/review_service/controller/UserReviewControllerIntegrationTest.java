package com.bkeuty.review_service.controller;

import com.bkeuty.review_service.dto.ReviewRequest;
import com.bkeuty.review_service.dto.ReviewResponse;
import com.bkeuty.review_service.dto.auth.TokenValidationResponseDto;
import com.bkeuty.review_service.service.AuthService;
import com.bkeuty.review_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserReviewController.class)
class UserReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private AuthService authService;

    // === IT_UPL_01: Giả lập Manager/User tải lên một tệp hình ảnh qua API ===

    @Test
    void uploadImage_ShouldReturn200AndImageUrl_WhenUserIsAuthorized() throws Exception {
        TokenValidationResponseDto validUser = TokenValidationResponseDto.builder().userId("user-123").userRole("USER").build();
        when(authService.validateToken("Bearer valid-token")).thenReturn(validUser);

        String mockCloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1/reviews/mock-image.jpg";
        MockMultipartFile file = new MockMultipartFile("file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());
        when(reviewService.uploadImage(any())).thenReturn(mockCloudinaryUrl);

        mockMvc.perform(multipart("/api/user/reviews/upload-image")
                        .file(file)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockCloudinaryUrl));
    }

    @Test
    void uploadImage_ShouldReturn401Unauthorized_WhenTokenIsInvalid() throws Exception {
        TokenValidationResponseDto invalidUser = TokenValidationResponseDto.builder().userId(null).userRole(null).build();
        when(authService.validateToken("Bearer invalid-token")).thenReturn(invalidUser);

        MockMultipartFile file = new MockMultipartFile("file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        mockMvc.perform(multipart("/api/user/reviews/upload-image")
                        .file(file)
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    // === Web Layer & Security Roles Tests ===

    @Test
    void createReview_ShouldReturn200_WhenUserIsAuthorized() throws Exception {
        TokenValidationResponseDto validUser = TokenValidationResponseDto.builder().userId("user-123").userRole("USER").build();
        when(authService.validateToken("Bearer valid-token")).thenReturn(validUser);

        ReviewResponse responseDto = ReviewResponse.builder()
                .id(1L)
                .userName("Nguyen A")
                .rating(5)
                .comment("Kha tot")
                .build();

        when(reviewService.createReview(eq("user-123"), any(ReviewRequest.class), eq("valid-token")))
                .thenReturn(responseDto);

        String requestJson = "{\"variantId\":101,\"rating\":5,\"comment\":\"Kha tot\",\"orderItemId\":10}";

        mockMvc.perform(post("/api/user/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Kha tot")));
    }

    @Test
    void createReview_ShouldReturn401Unauthorized_WhenRoleIsInvalid() throws Exception {
        // Role is "ADMIN" instead of "USER"
        TokenValidationResponseDto invalidRoleUser = TokenValidationResponseDto.builder().userId("user-123").userRole("ADMIN").build();
        when(authService.validateToken("Bearer admin-token")).thenReturn(invalidRoleUser);

        String requestJson = "{\"variantId\":101,\"rating\":5,\"comment\":\"Kha tot\",\"orderItemId\":10}";

        mockMvc.perform(post("/api/user/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isUnauthorized());
    }
}
