package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenRepository tokenRepository;

    @Test
    void createTokenWithExistUserId() {
        Mockito.when(userService.get(1L)).thenReturn(new User());
        RefreshToken token = new RefreshToken();
        Mockito.when(tokenRepository.save(any(RefreshToken.class))).thenReturn(token);
        RefreshToken actual = refreshTokenService.create(1L);
        Assertions.assertNotNull(actual);
    }

    @Test
    void getByTokenWithExistToken() {
        Mockito.when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(new RefreshToken()));
        RefreshToken actual = refreshTokenService.getByToken("token");
        Assertions.assertNotNull(actual);
    }

    @Test
    void getByTokenWithNotExistToken() {
        Mockito.when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.empty());
        Assertions.assertThrows(RefreshTokenException.class, () -> refreshTokenService.getByToken("token"));
    }
}