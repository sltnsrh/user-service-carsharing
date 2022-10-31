package com.intern.carsharing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.AuthTokenException;
import com.intern.carsharing.model.BlackList;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.BlacklistRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final RefreshTokenService refreshTokenService;
    private final AuthResponseBuilder responseBuilder;
    private final BlacklistRepository blacklistRepository;
    private final ObjectMapper objectMapper;

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
    public LoginResponseDto refreshToken(RefreshTokenRequestDto requestDto) {
        var refreshToken = refreshTokenService.resolveRefreshToken(requestDto.getToken());
        var user = refreshToken.getUser();
        var email = user.getEmail();
        var jwtToken = jwtTokenProvider.createToken(email, user.getRoles());
        return LoginResponseDto.builder()
                .userId(user.getId())
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
        jwtTokenProvider.validateToken(token);
        User user = userService.findByEmail(jwtTokenProvider.getUserName(token));
        var blackList = new BlackList();
        blackList.setUserId(user.getId());
        blackList.setJwtToken(token);
        if (!blacklistRepository.isLoggedOut(token)) {
            blacklistRepository.add(blackList);
        }
        refreshTokenService.checkAndDeleteOldRefreshTokens(user);
        return objectMapper.createObjectNode().put("message", "Logout successful!");
    }
}
