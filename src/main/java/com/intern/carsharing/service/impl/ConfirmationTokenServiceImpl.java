package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.ConfirmationTokenRepository;
import com.intern.carsharing.service.ConfirmationTokenService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private static final long TOKEN_EXPIRATION_PERIOD_MIN = 15;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Override
    public ConfirmationToken create(User user) {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        String token = UUID.randomUUID().toString();
        confirmationToken.setToken(token);
        confirmationToken.setUser(user);
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken
                .setExpiredAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_PERIOD_MIN));
        return confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public ConfirmationToken findByToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElse(null);
    }
}
