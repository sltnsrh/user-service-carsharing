package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.AuthTokenException;
import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ValidateTokenResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.BlacklistRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static final String BOB_USERNAME = "bob@gmail.com";
    private static final String USER_PASSWORD = "password";
    private static final String TOKEN = "token";
    private static final String REFRESH_TOKEN = "refreshtoken";
    private static final String BEARER_TOKEN = "Bearer token";
    private static final String SUCCESS_LOGOUT = "Logout successful";

    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private UserService userService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private BlacklistRepository blacklistRepository;
    @Spy
    private AuthResponseBuilder authResponseBuilder;
    @Spy
    private ObjectMapper objectMapper;

    @Test
    void loginWithValidData() {
        User user = new User();
        user.setId(1L);
        user.setEmail(BOB_USERNAME);
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.ACTIVE));
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        Mockito.when(jwtTokenProvider.createToken(BOB_USERNAME, user.getRoles()))
                .thenReturn(TOKEN);
        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                BOB_USERNAME, USER_PASSWORD))).thenReturn(null);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(REFRESH_TOKEN);
        Mockito.doNothing().when(refreshTokenService)
                .checkAndDeleteOldRefreshTokens(any(User.class));
        Mockito.when(refreshTokenService.create(user.getId())).thenReturn(refreshToken);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(BOB_USERNAME);
        loginRequestDto.setPassword(USER_PASSWORD);
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals(BOB_USERNAME, loginResponseDto.getEmail());
        Assertions.assertEquals(TOKEN, loginResponseDto.getToken());
        Assertions.assertEquals(REFRESH_TOKEN, loginResponseDto.getRefreshToken());
    }

    @Test
    void loginWithUserWithStatusInvalidate() {
        User user = new User();
        user.setId(1L);
        user.setEmail(BOB_USERNAME);
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.INVALIDATE));
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(BOB_USERNAME);
        loginRequestDto.setPassword(USER_PASSWORD);
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals(BOB_USERNAME, loginResponseDto.getEmail());
        Assertions
                .assertTrue(loginResponseDto.getMessage().contains("Your email wasn't confirmed"));
    }

    @Test
    void loginWithUserWithStatusBlocked() {
        User user = new User();
        user.setId(1L);
        user.setEmail(BOB_USERNAME);
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.BLOCKED));
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(BOB_USERNAME);
        loginRequestDto.setPassword(USER_PASSWORD);
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals(BOB_USERNAME, loginResponseDto.getEmail());
        Assertions.assertTrue(loginResponseDto.getMessage().contains("Your account was blocked"));
    }

    @Test
    void refreshAuthTokenWithValidRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken(REFRESH_TOKEN);
        User user = new User();
        user.setId(1L);
        user.setEmail(BOB_USERNAME);
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        refreshToken.setToken(REFRESH_TOKEN);
        Mockito.when(refreshTokenService.resolveRefreshToken(REFRESH_TOKEN))
                .thenReturn(refreshToken);
        Mockito.when(jwtTokenProvider.createToken(user.getEmail(), user.getRoles()))
                .thenReturn("newauthtoken");
        LoginResponseDto actual = authService.refreshToken(requestDto);
        Assertions.assertEquals(user.getEmail(), actual.getEmail());
        Assertions.assertEquals("newauthtoken", actual.getToken());
        Assertions.assertEquals(REFRESH_TOKEN, actual.getRefreshToken());
    }

    @Test
    void refreshAuthTokenWithExpiredRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken(REFRESH_TOKEN);
        Mockito.when(refreshTokenService.resolveRefreshToken(REFRESH_TOKEN))
                .thenThrow(RefreshTokenException.class);
        Assertions.assertThrows(RefreshTokenException.class,
                () -> authService.refreshToken(requestDto));
    }

    @Test
    void validateAuthTokenWithValidAndActiveUser() {
        Mockito.when(jwtTokenProvider.resolveToken(BEARER_TOKEN)).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(TOKEN)).thenReturn(BOB_USERNAME);
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        user.setStatus(new Status(1L, StatusType.ACTIVE));
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        Mockito.when(jwtTokenProvider.getRoleNames(
                new ArrayList<>(user.getRoles()))).thenReturn(List.of("USER"));
        ValidateTokenResponseDto actual = authService.validateAuthToken(BEARER_TOKEN);
        Assertions.assertEquals(1L, actual.getUserId());
        Assertions.assertEquals(List.of("USER"), actual.getRoles());
    }

    @Test
    void validateAuthTokenWithUserStatusBlocked() {
        Mockito.when(jwtTokenProvider.resolveToken(BEARER_TOKEN)).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(TOKEN)).thenReturn(BOB_USERNAME);
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        user.setStatus(new Status(1L, StatusType.BLOCKED));
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        Assertions.assertThrows(AuthTokenException.class,
                () -> authService.validateAuthToken(BEARER_TOKEN));
    }

    @Test
    void validateAuthTokenWithNotValidToken() {
        Mockito.when(jwtTokenProvider.resolveToken(BEARER_TOKEN)).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(false);
        Assertions.assertThrows(AuthTokenException.class,
                () -> authService.validateAuthToken(BEARER_TOKEN));
    }

    @Test
    void validateAuthTokenWithNotExistUser() {
        Mockito.when(jwtTokenProvider.resolveToken(BEARER_TOKEN)).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(TOKEN)).thenReturn(BOB_USERNAME);
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(null);
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> authService.validateAuthToken(BEARER_TOKEN));
    }

    @Test
    void logoutTestWithNewTokenInBlacklist() {
        Mockito.when(jwtTokenProvider.resolveToken(any(String.class))).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        User user = new User();
        user.setId(1L);
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        Mockito.when(jwtTokenProvider.getUserName(any(String.class))).thenReturn(BOB_USERNAME);
        Mockito.when(blacklistRepository.isLoggedOut(TOKEN)).thenReturn(false);
        var actual = authService.logout(TOKEN);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.toString().contains(SUCCESS_LOGOUT));
    }

    @Test
    void logoutTestWithExistingTokenInBlacklist() {
        Mockito.when(jwtTokenProvider.resolveToken(any(String.class))).thenReturn(TOKEN);
        Mockito.when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        User user = new User();
        user.setId(1L);
        Mockito.when(userService.findByEmail(BOB_USERNAME)).thenReturn(user);
        Mockito.when(jwtTokenProvider.getUserName(any(String.class))).thenReturn(BOB_USERNAME);
        Mockito.when(blacklistRepository.isLoggedOut(TOKEN)).thenReturn(true);
        var actual = authService.logout(TOKEN);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.toString().contains(SUCCESS_LOGOUT));
    }
}
