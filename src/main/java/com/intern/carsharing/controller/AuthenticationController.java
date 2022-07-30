package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.service.AuthService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<Object> register(@Valid @RequestBody RegistrationRequestUserDto requestUserDto) {
        return new ResponseEntity<>(authService.register(requestUserDto), HttpStatus.CREATED);
    }
}
