package com.bkeuty.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Value("${service-base-url.user-service}")
    private String userServiceUrl;
    @Value("${service-base-url.product-service}")
    private String productServiceUrl;
    @Value("${service-base-url.promotion-service}")
    private String promotionServiceUrl;
    @Value("${service-base-url.payment-service}")
    private String paymentServiceUrl;
    @Value("${service-base-url.shipping-service}")
    private String shippingServiceUrl;
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
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient.Builder defaultWebClientBuilder() {
        return WebClient.builder();
    }
    @Bean
    public WebClient productWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(productServiceUrl).build();
    }
    @Bean
    public WebClient authWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(authServiceUrl).build();
    }
    @Bean
    public WebClient shippingWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(shippingServiceUrl).build();
    }
    @Bean
    public WebClient paymentWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(paymentServiceUrl).build();
    }
    @Bean
    public WebClient userWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(userServiceUrl).build();
    }

    @Bean
    public WebClient GHNWebClient(@Qualifier("defaultWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl("https://dev-online-gateway.ghn.vn").build();
    }
    
    @Bean
    public WebClient promotionWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
        return builder.baseUrl(promotionServiceUrl).build();
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
