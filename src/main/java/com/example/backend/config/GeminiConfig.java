package com.example.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.gemini")
@Getter
@Setter
public class GeminiConfig {

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/";
    private String apiKey;
    private String chatModel = "gemini-1.5-flash-latest";
    private String embeddingModel = "gemini-embedding-2";
    private int timeout = 60000;
    private int maxTokens = 4000;
    private double temperature = 0.2;

    @PostConstruct
    public void validate() {
        boolean isConfigured = apiKey != null && !apiKey.isBlank();
        log.info("Gemini API key configured: {}", isConfigured);
        if (!isConfigured) {
            log.warn("Gemini API key is missing. AI features will fail.");
        }
    }
}