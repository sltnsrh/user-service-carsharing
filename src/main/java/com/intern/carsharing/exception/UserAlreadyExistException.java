package com.intern.carsharing.exception;

public class UserAlreadyExistException extends ArithmeticException {

    public UserAlreadyExistException(final String msg) {
        super(msg);
    }
}
