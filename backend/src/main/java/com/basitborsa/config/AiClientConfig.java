package com.basitborsa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiClientConfig {

    @Bean
    public WebClient pythonAiWebClient(
            @Value("${ai.service.base-url:http://localhost:8000}") String baseUrl,
            WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
