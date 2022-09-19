package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.request.ValidateTokenRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;

public interface AuthService {
    String register(RegistrationUserRequestDto requestUserDto);

    LoginResponseDto login(LoginRequestDto requestDto);

    String confirm(String token);

    String resendEmail(String email);

    LoginResponseDto refreshToken(RefreshTokenRequestDto requestDto);

    ValidateTokenResponseDto validateAuthToken(ValidateTokenRequestDto requestDto);
}
