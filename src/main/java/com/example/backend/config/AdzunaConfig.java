package com.example.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.adzuna")
@Getter
@Setter
public class AdzunaConfig {

    private String baseUrl = "https://api.adzuna.com/v1/api";
    private String appId;
    private String appKey;
    private String country = "gb";
    private int resultsPerPage = 20;
    private int timeout = 10000;

    @PostConstruct
    public void validate() {
        boolean isConfigured = appId != null && !appId.isBlank() && appKey != null && !appKey.isBlank();
        log.info("Adzuna credentials configured: {}", isConfigured);
        if (!isConfigured) {
            log.warn("Adzuna API credentials are missing. Job sync will fail.");
        }
    }
}