package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.response.EmailConfirmationResponseDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.RegistrationResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseBuilder {

    public RegistrationResponseDto getRegistrationResponseMessage(String token) {
        RegistrationResponseDto responseDto = new RegistrationResponseDto();
        responseDto.setMessage("Thanks for the registration! "
                + "The confirmation mail was sent on your email. "
                + "Please, confirm your email address to activate your account.");
        responseDto.setUrl("localhost:8080/confirm-email?token=" + token);
        return responseDto;
    }

    public LoginResponseDto getLoginInvalidateUserResponse(User user) {
        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .message("Your email wasn't confirmed yet. "
                        + "Confirm using the URL previously sent "
                        + "to you or use the link below to resend a new one")
                .resendUrl("localhost:8080/resend-confirmation-email?email=" + user.getEmail())
                .build();
    }

    public LoginResponseDto getLoginBlockedUserResponse(User user) {
        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .message("Your account was blocked. Try contacting the administrator.")
                .build();
    }

    public LoginResponseDto getLoginSuccessResponse(
            long userId, String email, String jwtToken, String refreshToken
    ) {
        return LoginResponseDto.builder()
                .userId(userId)
                .email(email)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public EmailConfirmationResponseDto getTokenExpiredMessage(String email) {
        EmailConfirmationResponseDto responseDto = new EmailConfirmationResponseDto();
        responseDto.setEmail(email);
        responseDto.setMessage("Confirmation token was expired. "
                + "Click resend email to get a new verification mail.");
        return responseDto;
    }

    public EmailConfirmationResponseDto getConfirmedSuccessfullyMessage(String email) {
        EmailConfirmationResponseDto responseDto = new EmailConfirmationResponseDto();
        responseDto.setEmail(email);
        responseDto.setMessage("Your email address was confirmed successfully! "
                + "Now you can log in to start work.");
        return responseDto;
    }
}
