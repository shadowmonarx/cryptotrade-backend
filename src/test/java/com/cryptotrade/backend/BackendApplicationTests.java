package com.cryptotrade.backend;

import org.junit.jupiter.api.Test;

// Removed @SpringBootTest — that annotation starts the full application context,
// which requires a live database and Firebase credentials.
// Railway runs tests during build (-DskipTests is set in our Dockerfile, but
// this is the safe baseline if someone runs mvn test locally without credentials).
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Placeholder — integration tests should use @SpringBootTest
        // with a test application.properties pointing to a test DB or H2.
    }
}
