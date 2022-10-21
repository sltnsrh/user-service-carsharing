package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.EmailConfirmationResponseDto;
import com.intern.carsharing.model.dto.response.RegistrationResponseDto;

public interface RegistrationService {
    RegistrationResponseDto register(RegistrationUserRequestDto requestUserDto);

    EmailConfirmationResponseDto confirmEmail(String token);

    RegistrationResponseDto resendEmail(String email);
}
