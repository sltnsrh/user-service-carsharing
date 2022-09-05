package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.ConfirmationTokenRepository;
import com.intern.carsharing.service.ConfirmationTokenService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    @Value("${confirmation.token.expiration.min}")
    private long expirationPeriodMin;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Override
    @Transactional
    public ConfirmationToken create(User user) {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        String token = UUID.randomUUID().toString();
        while (confirmationTokenRepository.findByToken(token).isPresent()) {
            token = UUID.randomUUID().toString();
        }
        confirmationToken.setToken(token);
        confirmationToken.setUser(user);
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken
                .setExpiredAt(LocalDateTime.now().plusMinutes(expirationPeriodMin));
        return confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public ConfirmationToken findByToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElse(null);
    }

    @Override
    public void setConfirmDate(ConfirmationToken confirmationToken) {
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public List<ConfirmationToken> findAllByUser(User user) {
        return confirmationTokenRepository.findAllByUser(user).orElse(null);
    }

    @Override
    public void delete(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.delete(confirmationToken);
    }
}
