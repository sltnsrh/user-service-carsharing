package com.intern.carsharing.service;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import java.util.List;

public interface ConfirmationTokenService {
    ConfirmationToken create(User user);

    ConfirmationToken findByToken(String token);

    void setConfirmDate(ConfirmationToken confirmationToken);

    List<ConfirmationToken> findAllByUser(User user);

    void delete(ConfirmationToken confirmationToken);
}
