package com.intern.carsharing.service;

import com.intern.carsharing.model.ConfirmationToken;

public interface ConfirmationTokenService {
    ConfirmationToken findByToken(String token);
}
