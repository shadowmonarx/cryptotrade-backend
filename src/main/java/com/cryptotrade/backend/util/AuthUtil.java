// src/main/java/com/cryptotrade/util/AuthUtil.java
// ─────────────────────────────────────────────────────────────────────
// Extracts the Firebase ID token from the Authorization header and
// verifies it. This is the reusable "auth middleware" for all controllers.
//
// Why not use a Filter?
//   For this MVP we keep it simple — call extractAndVerify() at the
//   top of each controller method. For a larger app, move this into
//   a OncePerRequestFilter that populates a SecurityContext.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.util;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.cryptotrade.backend.service.FirebaseTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthUtil {

    private final FirebaseTokenService firebaseTokenService;

    public AuthUtil(FirebaseTokenService firebaseTokenService) {
        this.firebaseTokenService = firebaseTokenService;
    }

    /**
     * Extracts the Bearer token from the Authorization header and verifies it.
     *
     * @param authorizationHeader  value of the "Authorization" request header
     * @return verified FirebaseToken containing uid, email, etc.
     * @throws ResponseStatusException 401 if header is missing/invalid
     * @throws ResponseStatusException 401 if token verification fails
     */
    public FirebaseToken extractAndVerify(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or malformed Authorization header. Expected: Bearer <token>"
            );
        }

        // Strip "Bearer " prefix (7 characters)
        String token = authorizationHeader.substring(7);

        try {
            return firebaseTokenService.verifyToken(token);
        } catch (FirebaseAuthException e) {
            // Token is expired, tampered, or invalid
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid Firebase token: " + e.getMessage()
            );
        }
    }
}