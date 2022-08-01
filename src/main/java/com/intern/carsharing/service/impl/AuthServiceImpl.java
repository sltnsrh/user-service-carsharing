package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final UserMapper mapper;


    @Override
    public ResponseUserDto register(RegistrationRequestUserDto requestUserDto) {
        String email = requestUserDto.getEmail();
        if (userExist(email)) {
            throw new UserAlreadyExistException("User with email " + email + " is already exist");
        }
        User user = mapper.toModel(requestUserDto);
        return mapper.toDto(userService.save(user));
    }

    private boolean userExist(String email) {
        User user = userService.findByEmail(email);
        return user != null;
    }
}
