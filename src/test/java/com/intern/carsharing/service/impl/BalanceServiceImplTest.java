package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.BalanceNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.repository.BalanceRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {
    @InjectMocks
    private BalanceServiceImpl balanceService;
    @Mock
    private BalanceRepository balanceRepository;

    @Test
    void findByUserIdWithExistUser() {
        Mockito.when(balanceRepository.findByUserId(1L)).thenReturn(Optional.of(new Balance()));
        Balance actual = balanceService.findByUserId(1L);
        Assertions.assertNotNull(actual);
    }

    @Test
    void findByUserIdWithNotExistUser() {
        Mockito.when(balanceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(BalanceNotFoundException.class,
                () -> balanceService.findByUserId(1L));
    }
}
