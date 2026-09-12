package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerMatch API")
                        .version("1.0.0")
                        .description("""
                                AI-powered career intelligence platform API.
                                
                                CareerMatch helps job seekers understand their fit for any job posting 
                                through transparent match scores, skill gap analysis, and actionable 
                                improvement recommendations.
                                """)
                        .contact(new Contact()
                                .name("CareerMatch Team")
                                .email("support@careermatch.com")
                                .url("https://careermatch.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://careermatch.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.careermatch.com").description("Production Server")
                ))
                .tags(List.of(
                        new Tag().name("Authentication").description("Authentication and authorization endpoints"),
                        new Tag().name("Job Discovery").description("Job search and discovery endpoints"),
                        new Tag().name("CV Management").description("CV upload, parsing, and management endpoints"),
                        new Tag().name("Match Analysis").description("AI-powered match analysis and recommendations"),
                        new Tag().name("User Management").description("User profile and settings endpoints")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT access token")));
    }
}