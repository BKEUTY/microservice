package com.bkeuty.product.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${service-base-url.auth-service}")
    private String authServiceUrl;
    @Value("${service-base-url.promotion-service}")
    private String promotionServiceUrl;
    @Value("${service-base-url.review-service}")
    private String reviewServiceUrl;
    @Value("${service-base-url.order-service}")
    private String orderServiceUrl;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
//    @LoadBalanced // This is the "bridge" between WebClient and Eureka
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder.baseUrl(authServiceUrl).build();
    }

    @Bean
    public WebClient promotionWebClient(WebClient.Builder builder) {
        return builder.baseUrl(promotionServiceUrl).build();
    }

    @Bean
    public WebClient reviewWebClient(WebClient.Builder builder) {
        return builder.baseUrl(reviewServiceUrl).build();
    }

    @Bean
    public WebClient orderWebClient(WebClient.Builder builder) {
        return builder.baseUrl(orderServiceUrl).build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
