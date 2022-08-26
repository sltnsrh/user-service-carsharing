package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;
    private final UserService userService;
    @Value("${jwt.refresh.token.expired.min}")
    private long expirationPeriod;

    @Override
    public RefreshToken create(Long id) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userService.get(id));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(expirationPeriod));
        return tokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken getById(Long id) {
        return null;
    }

    @Override
    public RefreshToken getByToken(String token) {
        return null;
    }
}
