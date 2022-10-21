package com.intern.carsharing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.AuthTokenException;
import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.DriverLicenceAlreadyExistException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.BlackList;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.EmailConfirmationResponseDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.RegistrationResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.BlackListService;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
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
    private final AuthResponseBuilder responseBuilder;
    private final BlackListService blackListService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RegistrationResponseDto register(RegistrationUserRequestDto requestUserDto) {
        String email = requestUserDto.getEmail();
        User user = userService.findByEmail(email);
        if (user != null) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        user = userService.findByDriverLicence(requestUserDto.getDriverLicence());
        if (user != null) {
            throw new DriverLicenceAlreadyExistException(requestUserDto.getDriverLicence()
                    + " licence number is already exists.");
        }
        user = getUserFromDtoWithEncodedPassword(requestUserDto);
        balanceService.createNewBalance(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return responseBuilder.getRegistrationResponseMessage(confirmationToken.getToken());
    }

    private User getUserFromDtoWithEncodedPassword(RegistrationUserRequestDto dto) {
        User user = userMapper.toModel(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.save(user);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String emailFromRequest = requestDto.getEmail();
        User user = userService.findByEmail(emailFromRequest);
        checkIfUserExists(user, emailFromRequest);
        String userStatus = user.getStatus().getStatusType().name();
        if (userStatus.equals(INVALIDATE_STATUS)) {
            return responseBuilder.getLoginInvalidateUserResponse(user);
        }
        if (userStatus.equals(BLOCKED_STATUS)) {
            return responseBuilder.getLoginBlockedUserResponse(user);
        }
        String email = user.getEmail();
        String password = requestDto.getPassword();
        authenticate(email, password);
        String jwtToken = jwtTokenProvider.createToken(email, user.getRoles());
        refreshTokenService.checkAndDeleteOldRefreshTokens(user);
        RefreshToken refreshToken = refreshTokenService.create(user.getId());
        return responseBuilder
                .getLoginSuccessResponse(user.getId(), email, jwtToken, refreshToken.getToken());
    }

    private void checkIfUserExists(User user, String emailFromRequest) {
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User with email: " + emailFromRequest + " doesn't exist"
            );
        }
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

    @Override
    @Transactional
    public EmailConfirmationResponseDto confirmEmail(String token) {
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
            return responseBuilder.getTokenExpiredMessage(email);
        }
        confirmationTokenService.setConfirmDate(confirmationToken);
        userService.changeStatus(confirmationToken.getUser().getId(), StatusType.ACTIVE);
        return responseBuilder.getConfirmedSuccessfullyMessage(email);
    }

    @Override
    @Transactional
    public RegistrationResponseDto resendEmail(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email: " + email + " doesn't exist");
        }
        checkIfUserStatusIsInvalidate(user, email);
        checkAndDeleteOldConfirmationTokens(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return responseBuilder.getRegistrationResponseMessage(confirmationToken.getToken());
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
        RefreshToken refreshToken = refreshTokenService.resolveRefreshToken(requestDto.getToken());
        String email = refreshToken.getUser().getEmail();
        Set<Role> roles = refreshToken.getUser().getRoles();
        String jwtToken = jwtTokenProvider.createToken(email, roles);
        return LoginResponseDto.builder()
                .userId(refreshToken.getUser().getId())
                .email(email)
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public ValidateTokenResponseDto validateAuthToken(String bearerToken) {
        String token = jwtTokenProvider.resolveToken(bearerToken);
        if (jwtTokenProvider.validateToken(token)) {
            String userName = jwtTokenProvider.getUserName(token);
            User user = userService.findByEmail(userName);
            checkIfUserExistsAndIsActive(user, userName);
            List<String> roles = jwtTokenProvider.getRoleNames(new ArrayList<>(user.getRoles()));
            ValidateTokenResponseDto responseDto = new ValidateTokenResponseDto();
            responseDto.setUserId(user.getId());
            responseDto.setRoles(roles);
            return responseDto;
        }
        throw new AuthTokenException("Jwt auth token not valid: " + bearerToken);
    }

    private void checkIfUserExistsAndIsActive(User user, String userName) {
        if (user == null) {
            throw new UsernameNotFoundException("Can't find user with username: " + userName);
        }
        if (!user.getStatus().getStatusType().equals(StatusType.ACTIVE)) {
            throw new AuthTokenException("Actual user isn't active: " + userName);
        }
    }

    @Override
    public Object logout(String bearerToken) {
        String token = jwtTokenProvider.resolveToken(bearerToken);
        if (jwtTokenProvider.validateToken(token)) {
            User user = userService.findByEmail(jwtTokenProvider.getUserName(token));
            var blackList = new BlackList();
            blackList.setUser(user);
            blackList.setJwtToken(token);
            blackList.setExpirationDate(jwtTokenProvider.getExpirationDate(token));
            blackListService.add(blackList);
            refreshTokenService.checkAndDeleteOldRefreshTokens(user);
            return objectMapper.createObjectNode().put("message", "Logout successful!");
        }
        return "logout fail";
    }
}
