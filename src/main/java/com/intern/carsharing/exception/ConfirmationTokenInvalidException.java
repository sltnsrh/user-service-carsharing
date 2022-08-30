package com.intern.carsharing.exception;

public class ConfirmationTokenInvalidException extends RuntimeException {
    public ConfirmationTokenInvalidException(String message) {
        super(message);
    }
}
