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

    // In production (Railway): set FIREBASE_SERVICE_ACCOUNT_JSON env var
    //   = full contents of firebase-service-account.json
    // Locally: leave env var blank and the file on classpath is used as fallback
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
     *   1. FIREBASE_SERVICE_ACCOUNT_JSON env var  (production / Railway)
     *   2. firebase-service-account.json on classpath  (local dev)
     */
    private InputStream resolveCredentials() {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            System.out.println("Firebase: loading credentials from env var");
            return new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
        }

        System.out.println("Firebase: loading credentials from classpath file");
        InputStream file = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        if (file == null) {
            throw new RuntimeException(
                "Firebase credentials not found. " +
                "Set FIREBASE_SERVICE_ACCOUNT_JSON env var or add " +
                "firebase-service-account.json to src/main/resources/"
            );
        }
        return file;
    }
}
