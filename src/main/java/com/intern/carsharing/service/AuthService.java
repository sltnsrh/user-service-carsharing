package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto requestDto);

    LoginResponseDto refreshToken(RefreshTokenRequestDto requestDto);

    ValidateTokenResponseDto validateAuthToken(String bearerToken);

    Object logout(String jwtToken);
}
