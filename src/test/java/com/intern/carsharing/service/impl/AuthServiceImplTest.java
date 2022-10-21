package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

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
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.BlackListService;
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
    private BlackListService blackListService;
    @Spy
    private AuthResponseBuilder authResponseBuilder;

    @Test
    void loginWithValidData() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.ACTIVE));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Mockito.when(jwtTokenProvider.createToken("bob@gmail.com", user.getRoles()))
                .thenReturn("token");
        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "bob@gmail.com", "password"))).thenReturn(null);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refreshtoken");
        Mockito.doNothing().when(refreshTokenService)
                .checkAndDeleteOldRefreshTokens(any(User.class));
        Mockito.when(refreshTokenService.create(user.getId())).thenReturn(refreshToken);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals("bob@gmail.com", loginResponseDto.getEmail());
        Assertions.assertEquals("token", loginResponseDto.getToken());
        Assertions.assertEquals("refreshtoken", loginResponseDto.getRefreshToken());
    }

    @Test
    void loginWithUserWithStatusInvalidate() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.INVALIDATE));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals("bob@gmail.com", loginResponseDto.getEmail());
        Assertions
                .assertTrue(loginResponseDto.getMessage().contains("Your email wasn't confirmed"));
    }

    @Test
    void loginWithUserWithStatusBlocked() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.BLOCKED));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals("bob@gmail.com", loginResponseDto.getEmail());
        Assertions.assertTrue(loginResponseDto.getMessage().contains("Your account was blocked"));
    }

    @Test
    void refreshAuthTokenWithValidRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        refreshToken.setToken("refreshtoken");
        Mockito.when(refreshTokenService.resolveRefreshToken("refreshtoken"))
                .thenReturn(refreshToken);
        Mockito.when(jwtTokenProvider.createToken(user.getEmail(), user.getRoles()))
                .thenReturn("newauthtoken");
        LoginResponseDto actual = authService.refreshToken(requestDto);
        Assertions.assertEquals(user.getEmail(), actual.getEmail());
        Assertions.assertEquals("newauthtoken", actual.getToken());
        Assertions.assertEquals("refreshtoken", actual.getRefreshToken());
    }

    @Test
    void refreshAuthTokenWithExpiredRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");
        Mockito.when(refreshTokenService.resolveRefreshToken("refreshtoken"))
                .thenThrow(RefreshTokenException.class);
        Assertions.assertThrows(RefreshTokenException.class,
                () -> authService.refreshToken(requestDto));
    }

    @Test
    void validateAuthTokenWithValidAndActiveUser() {
        String token = "Bearer token";
        String tokenAfterResolve = "token";
        Mockito.when(jwtTokenProvider.resolveToken(token)).thenReturn(tokenAfterResolve);
        Mockito.when(jwtTokenProvider.validateToken(tokenAfterResolve)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(tokenAfterResolve)).thenReturn("bob@gmail.com");
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        user.setStatus(new Status(1L, StatusType.ACTIVE));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Mockito.when(jwtTokenProvider.getRoleNames(
                new ArrayList<>(user.getRoles()))).thenReturn(List.of("USER"));
        ValidateTokenResponseDto actual = authService.validateAuthToken(token);
        Assertions.assertEquals(1L, actual.getUserId());
        Assertions.assertEquals(List.of("USER"), actual.getRoles());
    }

    @Test
    void validateAuthTokenWithUserStatusBlocked() {
        String token = "Bearer token";
        String tokenAfterResolve = "token";
        Mockito.when(jwtTokenProvider.resolveToken(token)).thenReturn(tokenAfterResolve);
        Mockito.when(jwtTokenProvider.validateToken(tokenAfterResolve)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(tokenAfterResolve)).thenReturn("bob@gmail.com");
        User user = new User();
        user.setId(1L);
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        user.setStatus(new Status(1L, StatusType.BLOCKED));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Assertions.assertThrows(AuthTokenException.class,
                () -> authService.validateAuthToken(token));
    }

    @Test
    void validateAuthTokenWithNotValidToken() {
        String token = "Bearer token";
        String tokenAfterResolve = "token";
        Mockito.when(jwtTokenProvider.resolveToken(token)).thenReturn(tokenAfterResolve);
        Mockito.when(jwtTokenProvider.validateToken(tokenAfterResolve)).thenReturn(false);
        Assertions.assertThrows(AuthTokenException.class,
                () -> authService.validateAuthToken(token));
    }

    @Test
    void validateAuthTokenWithNotExistUser() {
        String token = "Bearer token";
        String tokenAfterResolve = "token";
        Mockito.when(jwtTokenProvider.resolveToken(token)).thenReturn(tokenAfterResolve);
        Mockito.when(jwtTokenProvider.validateToken(tokenAfterResolve)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserName(tokenAfterResolve)).thenReturn("bob@gmail.com");
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> authService.validateAuthToken(token));
    }
}
