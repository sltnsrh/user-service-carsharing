package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;

    @Override
    public ResponseUserDto register(RegistrationRequestUserDto requestUserDto) {
        String email = requestUserDto.getEmail();
        if (userExist(email)) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        User user = userMapper.toModel(requestUserDto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userMapper.toDto(userService.save(user));
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String email = requestDto.getEmail();
        if (!userExist(email)) {
            throw new UsernameNotFoundException("User with email: " + email + " doesn't exist");
        }
        String password = requestDto.getPassword();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new UsernameNotFoundException("Wrong password, try again");
        }
        User user = userService.findByEmail(email);
        String jwtToken = jwtTokenProvider.createToken(email, user.getRoles());
        return new LoginResponseDto(email, jwtToken);
    }

    private boolean userExist(String email) {
        User user = userService.findByEmail(email);
        return user != null;
    }
}
