package com.intern.carsharing.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiExceptionObject {
    private String message;
    private HttpStatus httpStatus;
    private String timestamp;
}
