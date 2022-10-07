package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.BalanceResponseDto;
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
    void updateWithValidUser() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setEmail("newmail@gmail.com");
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findUserByEmail(userUpdateRequestDto.getEmail()))
                .thenReturn(Optional.of(user));
        user.setEmail(userUpdateRequestDto.getEmail());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        User actual = userService.update(1L, userUpdateRequestDto);
        Assertions.assertEquals(userUpdateRequestDto.getEmail(), actual.getEmail());
    }

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
        BalanceResponseDto actual = userService.toBalance(1L, balanceRequestDto);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.getMessage().contains("successfully"));
    }

    @Test
    void fromBalanceEnoughMoneyCase() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(100));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        Mockito.when(balanceService.save(any(Balance.class))).thenReturn(null);
        String actual = userService.fromBalance(1L, balanceRequestDto);
        Assertions.assertEquals("100 UAH were debited from the balance of the user with id 1",
                actual);
    }

    @Test
    void fromBalanceNotEnoughMoney() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(99));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        String actual = userService.fromBalance(1L, balanceRequestDto);
        Assertions.assertEquals("Not enough money on balance for a transaction",
                actual);
    }
}
