package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Authentication and registration")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @Operation(
            summary = "Registration a new user",
            description = "Allows to register a new user. "
                    + "As response user gets a message with confirmation email url.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "409", description = "Conflict")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    public ResponseEntity<Object> register(
            @Valid @RequestBody RegistrationRequestUserDto requestUserDto
    ) {
        return new ResponseEntity<>(authService.register(requestUserDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "User authentication",
            description = "Allows to authenticate a user. "
                    + "As response user gets username and JWT token",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        return new ResponseEntity<>(authService.login(requestDto), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<Object> confirm(@RequestParam String token) {
        return new ResponseEntity<>(authService.confirm(token), HttpStatus.OK);
    }

    @GetMapping("/resend")
    public ResponseEntity<Object> resend(@RequestParam String email) {
        return new ResponseEntity<>(authService.resendEmail(email), HttpStatus.OK);
    }
}
