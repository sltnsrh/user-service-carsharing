package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
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
    private final ConfirmationTokenService confirmationTokenService;
    private final StatusService statusService;

    @Override
    public String register(RegistrationRequestUserDto requestUserDto) {
        String email = requestUserDto.getEmail();
        User user = userService.findByEmail(email);
        if (user != null) {
            if (!user.getStatus()
                    .equals(statusService.findByStatusType(StatusType.INVALIDATE))) {
                throw new UserAlreadyExistException("User with email " + email + " already exists");
            }
            setUserUpdatesAndSave(user, requestUserDto);
        } else {
            user = getUserFromDtoWithEncodedPassword(requestUserDto);
        }
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return createResponseMessage(confirmationToken.getToken());
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

    @Override
    public ResponseUserDto confirm(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.findByToken(token);
        if (confirmationToken == null) {
            throw new ConfirmationTokenInvalidException("Confirmation token doesn't exist.");
        }
        if (confirmationToken.getConfirmedAt() != null) {
            throw new ConfirmationTokenInvalidException("Token was already confirmed.");
        }
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ConfirmationTokenInvalidException("Confirmation token was expired.");
        }
        confirmationTokenService.setConfirmDate(confirmationToken);
        User user = userService.changeStatus(confirmationToken.getUser(), StatusType.ACTIVE);
        return userMapper.toDto(user);
    }

    private boolean userExist(String email) {
        User user = userService.findByEmail(email);
        return user != null;
    }

    private String createResponseMessage(String token) {
        return "Thanks for the registration!" + System.lineSeparator()
                + "Confirm your email to activate your account." + System.lineSeparator()
                + System.lineSeparator()
                + "localhost:8080/confirm?token=" + token;
    }

    private User getUserFromDtoWithEncodedPassword(RegistrationRequestUserDto dto) {
        User user = userMapper.toModel(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.save(user);
    }

    private void setUserUpdatesAndSave(User user, RegistrationRequestUserDto dto) {
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setAge(dto.getAge());
        user.setDriverLicence(dto.getDriverLicence());
        userService.save(user);
    }
}
