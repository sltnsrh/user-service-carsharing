package com.intern.carsharing.exception;

import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @ExceptionHandler(value = {
            UserAlreadyExistException.class,
            DriverLicenceAlreadyExistException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUserExistException(
            RuntimeException e
    ) {
        HttpStatus status = HttpStatus.CONFLICT;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }

    private ApiExceptionObject getApiExceptionObject(String message, HttpStatus status) {
        return new ApiExceptionObject(
                message,
                status,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ApiExceptionObject> handleBadRequestException(
            MethodArgumentNotValidException e
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(getApiExceptionObject(errorMessages, status), status);
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            JwtException.class,
            LoginException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUsernameNotFoundException(
            RuntimeException e
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }

    @ExceptionHandler(value = {
            UserNotFoundException.class,
            CarNotFoundException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUserNorFoundException(RuntimeException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ApiExceptionObject> handleIllegalArgumentException(
            IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }

    @ExceptionHandler(value = {
            LimitedPermissionException.class,
            RefreshTokenException.class,
            AuthTokenException.class
    })
    public ResponseEntity<ApiExceptionObject> handleLimitedPermissionException(
            RuntimeException e
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }

    @ExceptionHandler(value = {
            ConfirmationTokenInvalidException.class,
            CarIsRentedException.class
    })
    public ResponseEntity<ApiExceptionObject> handleConfirmationTokenInvalidException(
            RuntimeException e
    ) {
        HttpStatus status = HttpStatus.ACCEPTED;
        return new ResponseEntity<>(getApiExceptionObject(e.getMessage(), status), status);
    }
}
