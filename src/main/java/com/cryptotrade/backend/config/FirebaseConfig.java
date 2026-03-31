package com.cryptotrade.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            String firebaseConfig = System.getenv("FIREBASE_CONFIG");

            if (firebaseConfig == null) {
                throw new RuntimeException("FIREBASE_CONFIG not set");
            }

            ByteArrayInputStream serviceAccount =
                    new ByteArrayInputStream(firebaseConfig.getBytes());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("🔥 Firebase initialized successfully");

        } catch (Exception e) {
            throw new RuntimeException("Firebase init failed", e);
        }
    }
}
