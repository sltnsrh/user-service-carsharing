package com.intern.carsharing.exception;

public class CarIsRentedException extends RuntimeException {
    public CarIsRentedException(String message) {
        super(message);
    }
}
