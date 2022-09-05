package com.intern.carsharing.service;

import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;

public interface BalanceService {
    Balance save(Balance balance);

    Balance findByUserId(Long id);

    Balance createNewBalance(User user);
}
