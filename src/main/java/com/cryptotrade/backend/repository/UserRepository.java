// src/main/java/com/cryptotrade/repository/UserRepository.java
// ─────────────────────────────────────────────────────────────────────
// Spring Data JPA generates all SQL automatically from method names.
// No SQL needed — just declare the method signature.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.repository;

import com.cryptotrade.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Spring generates: SELECT * FROM users WHERE firebase_uid = ?
     * Used to look up the user on every authenticated request.
     */
    Optional<AppUser> findByFirebaseUid(String firebaseUid);

    /**
     * Spring generates: SELECT * FROM users WHERE email = ?
     * Useful for admin lookups.
     */
    Optional<AppUser> findByEmail(String email);
}