package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.service.UserService;
import java.time.LocalDateTime;
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
    private static final String token = "refresh-token";
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;

    @Test
    void getByTokenWithExistToken() {
        Mockito.when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(new RefreshToken()));
        RefreshToken actual = refreshTokenService.findByToken(token);
        Assertions.assertNotNull(actual);
    }

    @Test
    void getByTokenWithNotExistToken() {
        Assertions.assertThrows(RefreshTokenException.class,
                () -> refreshTokenService.findByToken(token));
    }

    @Test
    void createRefreshTokenWithExistingSameInDb() {
        User user = new User();
        Mockito.when(userService.get(1L)).thenReturn(user);
        Mockito.when(refreshTokenRepository.findByToken(any(String.class)))
                .thenReturn(Optional.of(new RefreshToken()))
                .thenReturn(Optional.empty());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setId(1L);
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        refreshToken.setUser(user);
        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        RefreshToken actual = refreshTokenService.create(1L);
        Assertions.assertEquals(token, actual.getToken());
    }

    @Test
    void checkAndDeleteOldRefreshTokensWithNoTokensToDelete() {
        Mockito.when(refreshTokenRepository.findAllByUser(any(User.class)))
                        .thenReturn(Optional.empty());
        Assertions.assertDoesNotThrow(
                () -> refreshTokenService.checkAndDeleteOldRefreshTokens(new User()));
    }
}
