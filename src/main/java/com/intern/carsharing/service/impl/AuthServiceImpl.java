package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.ConfirmationTokenService;
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

    @Override
    public String register(RegistrationRequestUserDto requestUserDto) {
        String email = requestUserDto.getEmail();
        User user = userService.findByEmail(email);
        if (user != null) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        user = getUserFromDtoWithEncodedPassword(requestUserDto);
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
    public String confirm(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.findByToken(token);
        if (confirmationToken == null) {
            throw new ConfirmationTokenInvalidException("Confirmation token doesn't exist.");
        }
        String email = confirmationToken.getUser().getEmail();
        if (confirmationToken.getConfirmedAt() != null) {
            throw new ConfirmationTokenInvalidException(
                    "Email " + email + " was already confirmed."
            );
        }
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            return getTokenExpiredMessage(email);
        }
        confirmationTokenService.setConfirmDate(confirmationToken);
        userService.changeStatus(confirmationToken.getUser(), StatusType.ACTIVE);
        return "Your email address: " + email + " was confirmed successfully!";
    }

    @Override
    public String resendEmail(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email: " + email + " doesn't exist");
        }
        if (!user.getStatus().getStatusType().equals(StatusType.INVALIDATE)) {
            throw new ConfirmationTokenInvalidException(
                    "Your email " + email + " was already confirmed."
            );
        }
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return createResponseMessage(confirmationToken.getToken());
    }

    private boolean userExist(String email) {
        User user = userService.findByEmail(email);
        return user != null;
    }

    private String createResponseMessage(String token) {
        return "Thanks for the registration!"
                + System.lineSeparator()
                + "The confirmation mail was sent on your email. "
                + System.lineSeparator()
                + "Please, confirm your email address to activate your account."
                + System.lineSeparator().repeat(2)
                + "localhost:8080/confirm?token=" + token;
    }

    private User getUserFromDtoWithEncodedPassword(RegistrationRequestUserDto dto) {
        User user = userMapper.toModel(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.save(user);
    }

    private String getTokenExpiredMessage(String email) {
        return "Confirmation token was expired."
                + System.lineSeparator()
                + "Follow the link to get a new verification mail: "
                + System.lineSeparator()
                + "localhost:8080/resend?email=" + email;
    }
}
