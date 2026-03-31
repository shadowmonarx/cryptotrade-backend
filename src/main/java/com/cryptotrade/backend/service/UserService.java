// src/main/java/com/cryptotrade/service/UserService.java
// ─────────────────────────────────────────────────────────────────────
// Business logic for user management.
//
// findOrCreate() is the key pattern:
//   - First request from a new user → create row in PostgreSQL
//   - Every subsequent request    → fetch existing row
//
// @Transactional ensures the DB operation is atomic — if anything
// fails mid-way, the whole thing rolls back.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.service;

import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds the user by Firebase UID, or creates them if this is their first login.
     * Called on every authenticated request after token verification.
     *
     * @param firebaseUid  uid from the verified Firebase token
     * @param email        email from the verified Firebase token
     * @return existing or newly created AppUser
     */
    @Transactional
    public AppUser findOrCreate(String firebaseUid, String email) {
        return userRepository
                .findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    // First time this user hits our backend — create their DB record
                    AppUser newUser = new AppUser(firebaseUid, email);
                    return userRepository.save(newUser);
                });
    }

    /**
     * Fetches a user by Firebase UID. Throws if not found.
     * Use for endpoints where the user MUST already exist.
     */
    @Transactional(readOnly = true)
    public AppUser getByFirebaseUid(String firebaseUid) {
        return userRepository
                .findByFirebaseUid(firebaseUid)
                .orElseThrow(() ->
                        new RuntimeException("User not found for uid: " + firebaseUid)
                );
    }
}