package com.intern.carsharing.exception;

public class LimitedPermissionException extends RuntimeException {
    public LimitedPermissionException(String message) {
        super(message);
    }
}
