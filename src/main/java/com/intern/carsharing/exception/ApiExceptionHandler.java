package com.intern.carsharing.exception;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {UserAlreadyExistException.class})
    public ResponseEntity<Object> handleUserExistException(UserAlreadyExistException e) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                conflict,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(apiExceptionObject, conflict);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleBadRequestException(MethodArgumentNotValidException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        String errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                errorMessages,
                badRequest,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(apiExceptionObject, badRequest);
    }
}
