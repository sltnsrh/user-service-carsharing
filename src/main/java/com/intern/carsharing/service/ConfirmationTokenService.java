package com.intern.carsharing.service;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;

public interface ConfirmationTokenService {
    ConfirmationToken create(User user);

    ConfirmationToken findByToken(String token);

    void setConfirmDate(ConfirmationToken confirmationToken);
}
