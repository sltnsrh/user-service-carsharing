package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;

public interface AuthService {
    ResponseUserDto register(RegistrationRequestUserDto requestUserDto);

    LoginResponseDto login(LoginRequestDto requestDto);
}
