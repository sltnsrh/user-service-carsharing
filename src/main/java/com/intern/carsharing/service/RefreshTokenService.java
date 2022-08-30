package com.intern.carsharing.service;

import com.intern.carsharing.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken create(Long id);

    RefreshToken getByToken(String token);

    void delete(RefreshToken refreshToken);
}