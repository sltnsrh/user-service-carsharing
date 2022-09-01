package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;

public interface AuthService {
    String register(RegistrationUserRequestDto requestUserDto);

    LoginResponseDto login(LoginRequestDto requestDto);

    String confirm(String token);

    String resendEmail(String email);
}
