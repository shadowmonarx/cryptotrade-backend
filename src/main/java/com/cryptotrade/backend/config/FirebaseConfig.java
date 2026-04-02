package com.cryptotrade.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    // Bound from application.properties: firebase.service-account-json
    // which is set from FIREBASE_CONFIG env var in Railway
    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream stream = resolveCredentials();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully");
            } catch (Exception e) {
                throw new RuntimeException("Firebase init failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Priority:
     *   1. FIREBASE_CONFIG env var (injected via firebase.service-account-json property)
     *   2. firebase-service-account.json file on classpath (local dev fallback)
     */
    private InputStream resolveCredentials() {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            System.out.println("Firebase: loading credentials from env var (FIREBASE_CONFIG)");
            return new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
        }

        System.out.println("Firebase: loading credentials from classpath file (local dev)");
        InputStream file = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        if (file == null) {
            throw new RuntimeException(
                "Firebase credentials not found. " +
                "Set FIREBASE_CONFIG env var in Railway, or add " +
                "firebase-service-account.json to src/main/resources/ for local dev."
            );
        }
        return file;
    }
}
