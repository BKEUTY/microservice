package com.bkeuty.product.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {
    @Bean
    @LoadBalanced // This is the "bridge" between WebClient and Eureka
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder.baseUrl("http://auth-service").build();
    }
}
