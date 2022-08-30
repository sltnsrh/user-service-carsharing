package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.BalanceNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.BalanceRepository;
import com.intern.carsharing.service.BalanceService;
import java.math.BigDecimal;
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
        return balanceRepository.findByUserId(id).orElseThrow(
                () -> new BalanceNotFoundException("Balance with user id: "
                + id + " wasn't found.")
        );
    }

    @Override
    public Balance createNewBalance(User user) {
        Balance balance = new Balance();
        balance.setUser(user);
        balance.setCurrency("UAH");
        balance.setValue(BigDecimal.valueOf(0.00));
        return save(balance);
    }
}
