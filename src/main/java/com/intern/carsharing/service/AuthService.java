package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;

public interface AuthService {
    ResponseUserDto register(RegistrationRequestUserDto requestUserDto);
}
