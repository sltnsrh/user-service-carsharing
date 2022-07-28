package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;

public interface AuthService {
    User register(RegistrationRequestUserDto requestUserDto);
}
