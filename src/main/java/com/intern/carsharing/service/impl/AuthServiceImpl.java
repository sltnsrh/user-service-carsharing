package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.AuthTokenException;
import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.request.ValidateTokenRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String INVALIDATE_STATUS = "INVALIDATE";
    private static final String BLOCKED_STATUS = "BLOCKED";
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final ConfirmationTokenService confirmationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public String register(RegistrationUserRequestDto requestUserDto) {
        String email = requestUserDto.getEmail();
        User user = userService.findByEmail(email);
        if (user != null) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        user = getUserFromDtoWithEncodedPassword(requestUserDto);
        balanceService.createNewBalance(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return getRegistrationResponseMessage(confirmationToken.getToken());
    }

    private User getUserFromDtoWithEncodedPassword(RegistrationUserRequestDto dto) {
        User user = userMapper.toModel(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.save(user);
    }

    private String getRegistrationResponseMessage(String token) {
        return "Thanks for the registration!"
                + System.lineSeparator()
                + "The confirmation mail was sent on your email. "
                + System.lineSeparator()
                + "Please, confirm your email address to activate your account."
                + System.lineSeparator().repeat(2)
                + "localhost:8080/confirm?token=" + token;
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String emailFromRequest = requestDto.getEmail();
        User user = userService.findByEmail(emailFromRequest);
        checkIfUserExists(user, emailFromRequest);
        String userStatus = user.getStatus().getStatusType().name();
        if (userStatus.equals(INVALIDATE_STATUS)) {
            return getLoginInvalidateUserResponse(user);
        }
        if (userStatus.equals(BLOCKED_STATUS)) {
            return getLoginBlockedUserResponse(user);
        }
        String email = user.getEmail();
        String password = requestDto.getPassword();
        authenticate(email, password);
        String jwtToken = jwtTokenProvider.createToken(email, user.getRoles());
        checkAndDeleteOldRefreshTokens(user);
        RefreshToken refreshToken = refreshTokenService.create(user.getId());
        return getLoginSuccessResponse(email, jwtToken, refreshToken.getToken());
    }

    private void checkIfUserExists(User user, String emailFromRequest) {
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User with email: " + emailFromRequest + " doesn't exist"
            );
        }
    }

    private LoginResponseDto getLoginInvalidateUserResponse(User user) {
        return LoginResponseDto.builder()
                .email(user.getEmail())
                .message("Your email wasn't confirmed yet. "
                        + "Confirm using the URL previously sent "
                        + "to you or use the link below to resend a new one")
                .resendUrl("localhost:8080/resend?email=" + user.getEmail())
                .build();
    }

    private LoginResponseDto getLoginBlockedUserResponse(User user) {
        return LoginResponseDto.builder()
                .email(user.getEmail())
                .message("Your account was blocked. Try contacting the administrator.")
                .build();
    }

    private void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new UsernameNotFoundException("Wrong password, try again");
        }
    }

    private void checkAndDeleteOldRefreshTokens(User user) {
        List<RefreshToken> refreshTokenList = refreshTokenService.findByUser(user);
        if (refreshTokenList != null) {
            refreshTokenList.forEach(refreshTokenService::delete);
        }
    }

    private LoginResponseDto getLoginSuccessResponse(
            String email, String jwtToken, String refreshToken
    ) {
        return LoginResponseDto.builder()
                .email(email)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
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
        userService.changeStatus(confirmationToken.getUser().getId(), StatusType.ACTIVE);
        return "Your email address: " + email + " was confirmed successfully!";
    }

    private String getTokenExpiredMessage(String email) {
        return "Confirmation token was expired."
                + System.lineSeparator()
                + "Follow the link to get a new verification mail: "
                + System.lineSeparator()
                + "localhost:8080/resend?email=" + email;
    }

    @Override
    @Transactional
    public String resendEmail(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email: " + email + " doesn't exist");
        }
        checkIfUserStatusIsInvalidate(user, email);
        checkAndDeleteOldConfirmationTokens(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return getRegistrationResponseMessage(confirmationToken.getToken());
    }

    private void checkIfUserStatusIsInvalidate(User user, String email) {
        if (!user.getStatus().getStatusType().equals(StatusType.INVALIDATE)) {
            throw new ConfirmationTokenInvalidException(
                    "Your email " + email + " was already confirmed."
            );
        }
    }

    private void checkAndDeleteOldConfirmationTokens(User user) {
        List<ConfirmationToken> confirmationTokenList =
                confirmationTokenService.findAllByUser(user);
        if (confirmationTokenList != null) {
            confirmationTokenList.forEach(confirmationTokenService::delete);
        }
    }

    @Override
    @Transactional
    public LoginResponseDto refreshToken(RefreshTokenRequestDto requestDto) {
        RefreshToken refreshToken = resolveRefreshToken(requestDto.getToken());
        String email = refreshToken.getUser().getEmail();
        Set<Role> roles = refreshToken.getUser().getRoles();
        String jwtToken = jwtTokenProvider.createToken(email, roles);
        return LoginResponseDto.builder()
                .email(email)
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private RefreshToken resolveRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenService.findByToken(token);
        if (refreshToken.getExpiredAt().isBefore(LocalDateTime.now(ZoneId.systemDefault()))) {
            throw new RefreshTokenException("Refresh token was expired. Please, make a new login.");
        }
        return refreshToken;
    }

    @Override
    public ValidateTokenResponseDto validateAuthToken(ValidateTokenRequestDto requestDto) {
        String token = jwtTokenProvider.resolveToken(requestDto.getToken());
        if (jwtTokenProvider.validateToken(token)) {
            String userName = jwtTokenProvider.getUserName(token);
            User user = userService.findByEmail(userName);
            if (user == null) {
                throw new UsernameNotFoundException("Can't find user with username: " + userName);
            }
            if (!user.getStatus().getStatusType().equals(StatusType.ACTIVE)) {
                throw new AuthTokenException("Actual user isn't active: " + userName);
            }
            List<String> roles = jwtTokenProvider.getRoleNames(new ArrayList<>(user.getRoles()));
            ValidateTokenResponseDto responseDto = new ValidateTokenResponseDto();
            responseDto.setUserId(user.getId());
            responseDto.setRoles(roles);
            return responseDto;
        }
        throw new AuthTokenException("Jwt auth token not valid: " + requestDto.getToken());
    }
}
