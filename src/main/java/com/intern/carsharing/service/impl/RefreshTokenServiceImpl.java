package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        refreshToken.setExpiredAt(LocalDateTime.now(ZoneId.systemDefault())
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
}
