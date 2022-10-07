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

    @ExceptionHandler(value = {
            UserAlreadyExistException.class,
            DriverLicenceAlreadyExistException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUserExistException(
            RuntimeException e
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

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ResponseEntity<ApiExceptionObject> handleUsernameNotFoundException(
            UsernameNotFoundException e
    ) {
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                unauthorized,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, unauthorized);
    }

    @ExceptionHandler(value = {
            UserNotFoundException.class,
            CarNotFoundException.class
    })
    public ResponseEntity<ApiExceptionObject> handleUserNorFoundException(RuntimeException e) {
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
            RefreshTokenException.class,
            AuthTokenException.class
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

    @ExceptionHandler(value = {
            ConfirmationTokenInvalidException.class,
            CarIsRentedException.class
    })
    public ResponseEntity<ApiExceptionObject> handleConfirmationTokenInvalidException(
            RuntimeException e
    ) {
        HttpStatus accepted = HttpStatus.ACCEPTED;
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(),
                accepted,
                ZonedDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, accepted);
    }
}
