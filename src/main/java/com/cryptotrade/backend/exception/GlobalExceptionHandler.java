// src/main/java/com/cryptotrade/exception/GlobalExceptionHandler.java
// ─────────────────────────────────────────────────────────────────────
// Converts exceptions thrown anywhere in the app into clean JSON responses.
// Without this, Spring returns a generic 500 HTML page for unhandled errors.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Insufficient funds
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Invalid asset, invalid amount, unsupported asset
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 401 Unauthorized (from AuthUtil / ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    // Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error",     message,
                "status",    status.value(),
                "timestamp", Instant.now().toString()
        ));
    }
}