// src/main/java/com/cryptotrade/model/AppUser.java
// ─────────────────────────────────────────────────────────────────────
// JPA Entity mapped to the "users" table in PostgreSQL.
//
// Why store firebaseUid?
//   Firebase UID is stable — it never changes even if the user changes
//   their email. We use it as the link between Firebase and our DB.
//
// Note: Class is named AppUser (not User) to avoid conflicts with
//       any SQL reserved word "user" and Spring Security's UserDetails.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data                 // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor    // Lombok: JPA requires a no-arg constructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Firebase UID — unique, never changes, used to look up the user */
    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Starting balance in USD.
     * BigDecimal is required for monetary values — never use float/double.
     */
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal balance = BigDecimal.valueOf(10_000); // $10,000 demo balance

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /** Convenience constructor used in UserService.findOrCreate() */
    public AppUser(String firebaseUid, String email) {
        this.firebaseUid = firebaseUid;
        this.email = email;
    }
    
}