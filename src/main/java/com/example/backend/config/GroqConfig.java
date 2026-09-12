package com.example.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.groq")
@Getter
@Setter
public class GroqConfig {

    private String baseUrl = "https://api.groq.com/openai/v1";
    private String apiKey;
    private String chatModel = "openai/gpt-oss-20b";
    private double temperature = 0.2;

    @PostConstruct
    public void validate() {
        boolean isConfigured = apiKey != null && !apiKey.isBlank();
        log.info("Groq API key configured: {}", isConfigured);
        if (!isConfigured) {
            log.warn("Groq API key is missing. LLM parsing features will fail.");
        }
    }
}