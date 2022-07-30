package com.intern.carsharing.exception;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ApiExceptionObject {
    private final String message;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
}
