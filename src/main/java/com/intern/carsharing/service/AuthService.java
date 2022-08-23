package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;

public interface AuthService {
    String register(RegistrationRequestUserDto requestUserDto);

    LoginResponseDto login(LoginRequestDto requestDto);
}
