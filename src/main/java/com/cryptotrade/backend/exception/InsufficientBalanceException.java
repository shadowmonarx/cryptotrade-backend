// src/main/java/com/cryptotrade/exception/InsufficientBalanceException.java
package com.cryptotrade.backend.exception;

/** Thrown when the user tries to spend more than their available balance. */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}