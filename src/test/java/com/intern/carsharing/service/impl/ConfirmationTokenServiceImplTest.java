package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.ConfirmationTokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceImplTest {
    @InjectMocks
    private ConfirmationTokenServiceImpl tokenService;
    @Mock
    private ConfirmationTokenRepository tokenRepository;

    @Test
    void createWithExistingUuidAndRegenerateNewOne() {
        Mockito.when(tokenRepository.findByToken(any(String.class)))
                .thenReturn(Optional.of(new ConfirmationToken()))
                .thenReturn(Optional.empty());
        ConfirmationToken token = new ConfirmationToken();
        token.setToken("token");
        Mockito.when(tokenRepository.save(any(ConfirmationToken.class))).thenReturn(token);
        ConfirmationToken actual = tokenService.create(new User());
        Assertions.assertEquals(token.getToken(), actual.getToken());
    }
}
