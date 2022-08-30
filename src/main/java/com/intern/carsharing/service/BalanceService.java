package com.intern.carsharing.service;

import com.intern.carsharing.model.Balance;

public interface BalanceService {
    Balance save(Balance balance);

    Balance findByUserId(Long id);
}
