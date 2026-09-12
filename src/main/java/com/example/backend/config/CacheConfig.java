package com.example.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.jobs-ttl:3600}")
    private long jobsTtl;

    @Value("${app.cache.skills-ttl:86400}")
    private long skillsTtl;

    @Value("${app.cache.profiles-ttl:1800}")
    private long profilesTtl;

    @Value("${app.cache.max-size:1000}")
    private long maxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "jobs", "skills", "profiles", "adzuna"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(jobsTtl, TimeUnit.SECONDS)
                .recordStats());
        return cacheManager;
    }
}