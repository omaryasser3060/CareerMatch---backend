package com.example.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.gemini")
@Getter
@Setter
public class GeminiConfig {

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/";
    private String apiKey;
    private String chatModel = "gemini-2.5-flash";
    private String embeddingModel = "gemini-embedding-001";
    private int timeout = 60000;
    private int maxTokens = 4000;
    private double temperature = 0.2;
}