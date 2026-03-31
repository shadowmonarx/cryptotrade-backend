// src/main/java/com/cryptotrade/service/FirebaseTokenService.java
// ─────────────────────────────────────────────────────────────────────
// Single responsibility: verify a Firebase ID token and return its claims.
//
// How token verification works:
//   1. Firebase Admin SDK contacts Google's public key endpoint
//   2. Validates the JWT signature, expiry, audience, and issuer
//   3. Returns a FirebaseToken with uid, email, name, etc.
//   4. Throws FirebaseAuthException if anything is wrong
//
// This is the most important security component in the backend.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseTokenService {

    /**
     * Verifies the Firebase ID token and returns its decoded claims.
     *
     * @param idToken  raw token from "Authorization: Bearer <token>" header
     * @return decoded FirebaseToken (contains uid, email, name, etc.)
     * @throws FirebaseAuthException if token is invalid, expired, or tampered
     */
    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        // FirebaseAuth.getInstance() is the Admin SDK singleton initialized in FirebaseConfig
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}