package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private BalanceService balanceService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StatusService statusService;

    @Test
    void changeStatusOfExistingUser() {
        Status statusToSet = new Status(1L, StatusType.BLOCKED);
        User user = new User();
        user.setStatus(new Status(2L, StatusType.ACTIVE));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(statusService.findByStatusType(StatusType.BLOCKED)).thenReturn(statusToSet);
        user.setStatus(statusToSet);
        Mockito.when(userService.save(any(User.class))).thenReturn(user);
        User actual = userService.changeStatus(1L, StatusType.BLOCKED);
        Assertions.assertEquals(StatusType.BLOCKED, actual.getStatus().getStatusType());
    }

    @Test
    void toBalancePutMoneyOnExistBalance() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(0));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        Mockito.when(balanceService.save(any(Balance.class))).thenReturn(null);
        Mockito.mock(permissionService.getClass());
        String actual = userService.toBalance(1L, balanceRequestDto);
        Assertions.assertNotNull(actual);
    }

}