package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.BalanceNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.repository.BalanceRepository;
import com.intern.carsharing.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {
    private final BalanceRepository balanceRepository;

    @Override
    public Balance save(Balance balance) {
        return balanceRepository.save(balance);
    }

    @Override
    public Balance findByUserId(Long id) {
        return balanceRepository.findByUser(id).orElseThrow(
                () -> new BalanceNotFoundException("Balance with user id: "
                + id + " wasn't found.")
        );
    }
}
