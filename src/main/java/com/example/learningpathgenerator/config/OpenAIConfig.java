package com.example.learningpathgenerator.config;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OpenAIConfig {

    public OpenAIConfig() {
        log.info("OpenAI Configuration initialized");
    }
}