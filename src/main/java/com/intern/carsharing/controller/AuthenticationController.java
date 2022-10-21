package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.RegistrationService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Authentication and registration")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthService authService;
    private final RegistrationService registrationService;

    @Operation(
            summary = "Registration a new user",
            description = "Allows to register a new user. "
                    + "As response user gets a message that confirmation email was sent.",
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
            @Valid @RequestBody RegistrationUserRequestDto requestUserDto
    ) {
        return new ResponseEntity<>(registrationService.register(requestUserDto),
                HttpStatus.CREATED);
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

    @Operation(
            summary = "User email confirmation",
            description = "Allows to confirm user email. "
                    + "As response user gets message that email was successfully confirmed.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/confirm-email")
    public ResponseEntity<Object> confirmEmail(@RequestParam String token) {
        return new ResponseEntity<>(registrationService.confirmEmail(token), HttpStatus.OK);
    }

    @Operation(
            summary = "User email confirmation resend",
            description = "Allows to resend a confirmation email to a user. "
                    + "As response user gets message that confirmation email was sent.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/resend-confirmation-email")
    public ResponseEntity<Object> resendEmail(@RequestParam String email) {
        return new ResponseEntity<>(registrationService.resendEmail(email), HttpStatus.OK);
    }

    @Operation(
            summary = "Refresh user authentication token",
            description = "Allows to refresh user authentication token. "
                    + "As response user gets json body with username, "
                    + "new authentication token and refresh token.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @RequestBody RefreshTokenRequestDto requestDto
    ) {
        return new ResponseEntity<>(authService.refreshToken(requestDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Validate user authentication token",
            description = "Allows to validate user authentication token. "
                    + "As response user gets user id and list of user roles. "
                    + "If the token is not valid or user status isn't ACTIVE, "
                    + "it will be thrown a response with 403 Forbidden. "
                    + "Request should be an authorized.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ValidateTokenResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @GetMapping("/validate-auth-token")
    public ResponseEntity<ValidateTokenResponseDto> validateToken(
            @RequestHeader("Authorization") String bearerToken
    ) {
        return new ResponseEntity<>(authService.validateAuthToken(bearerToken), HttpStatus.OK);
    }

    @Operation(
            summary = "User logout",
            description = "Allows to logout user from the system",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @GetMapping ("/user-logout")
    public ResponseEntity<Object> logout(
            @RequestHeader("Authorization") String bearerToken) {
        return new ResponseEntity<>(authService.logout(bearerToken), HttpStatus.OK);
    }
}
