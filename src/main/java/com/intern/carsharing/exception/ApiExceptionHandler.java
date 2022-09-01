package com.intern.carsharing.exception;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {UserAlreadyExistException.class})
    public ResponseEntity<ApiExceptionObject> handleUserExistException(
            UserAlreadyExistException e
    ) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                conflict,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, conflict);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ApiExceptionObject> handleBadRequestException(
            MethodArgumentNotValidException e
    ) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        String errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                errorMessages,
                badRequest,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, badRequest);
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            ConfirmationTokenInvalidException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUsernameNotFoundException(
            RuntimeException e
    ) {
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                unauthorized,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, unauthorized);
    }

    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<ApiExceptionObject> handleUserNorFoundException(UserNotFoundException e) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                notFound,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, notFound);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ApiExceptionObject> handleIllegalArgumentException(
            IllegalArgumentException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                badRequest,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, badRequest);
    }

    @ExceptionHandler(value = {
            LimitedPermissionException.class,
            RefreshTokenException.class
    })
    public ResponseEntity<ApiExceptionObject> handleLimitedPermissionException(
            RuntimeException e
    ) {
        HttpStatus forbidden = HttpStatus.FORBIDDEN;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                forbidden,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, forbidden);
    }
}
