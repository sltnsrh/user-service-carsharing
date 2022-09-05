package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.repository.RefreshTokenRepository;
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

    @Test
    void getByTokenWithExistToken() {
        Mockito.when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(new RefreshToken()));
        RefreshToken actual = refreshTokenService.findByToken("token");
        Assertions.assertNotNull(actual);
    }

    @Test
    void getByTokenWithNotExistToken() {
        Assertions.assertThrows(RefreshTokenException.class,
                () -> refreshTokenService.findByToken("token"));
    }
}
