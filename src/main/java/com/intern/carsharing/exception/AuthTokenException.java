package com.intern.carsharing.exception;

public class AuthTokenException extends RuntimeException {
    public AuthTokenException(String message) {
        super(message);
    }
}
