package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;
    private final UserService userService;
    @Value("${jwt.refresh.token.expired.min}")
    private long expirationPeriod;

    @Override
    @Transactional
    public RefreshToken create(Long id) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userService.get(id));
        String token = UUID.randomUUID().toString();
        while (tokenRepository.findByToken(token).isPresent()) {
            token = UUID.randomUUID().toString();
        }
        refreshToken.setToken(token);
        refreshToken.setExpiredAt(LocalDateTime.now()
                .plusMinutes(expirationPeriod));
        return tokenRepository.save(refreshToken);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        tokenRepository.delete(refreshToken);
    }

    @Override
    public RefreshToken findByToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException(
                        "Refresh token: " + token + " wasn't found in a DB."
                ));
    }

    @Override
    public List<RefreshToken> findByUser(User user) {
        return tokenRepository.findAllByUser(user).orElse(null);
    }

    @Override
    public RefreshToken resolveRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        if (refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenException("Refresh token was expired. Please, make a new login.");
        }
        return refreshToken;
    }

    @Override
    public void checkAndDeleteOldRefreshTokens(User user) {
        List<RefreshToken> refreshTokenList = findByUser(user);
        if (refreshTokenList != null) {
            refreshTokenList.forEach(this::delete);
        }
    }
}
