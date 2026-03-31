// src/main/java/com/cryptotrade/config/CorsConfig.java
// ─────────────────────────────────────────────────────────────
// Allows the React dev server (localhost:5173) to call the backend.
// Without this the browser blocks the request with a CORS error.
// In production, change cors.allowed-origins to your real domain.
// ─────────────────────────────────────────────────────────────
package com.cryptotrade.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false); // We use Bearer tokens, not cookies
            }
        };
    }
}