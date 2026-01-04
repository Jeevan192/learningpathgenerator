package com.example.learningpathgenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class OpenAIConfig {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("OpenAI Configuration initialized");
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here")) {
            log.info("AI Service Configured Successfully.");
        } else {
            log.warn("AI Service NOT configured - Mock responses will be used.");
        }
    }
}

