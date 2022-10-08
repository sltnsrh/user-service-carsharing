package com.intern.carsharing.service;

import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import java.util.List;

public interface RefreshTokenService {
    RefreshToken create(Long id);

    RefreshToken findByToken(String token);

    List<RefreshToken> findByUser(User user);

    void delete(RefreshToken refreshToken);

    RefreshToken resolveRefreshToken(String token);

    void checkAndDeleteOldRefreshTokens(User user);
}
