package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.BalanceResponseDto;
import com.intern.carsharing.model.util.StatusType;

public interface UserService {
    User findByEmail(String email);

    User findByDriverLicence(String driverLicence);

    User save(User user);

    User get(Long id);

    User update(Long id, UserUpdateRequestDto updateDto);

    User changeStatus(Long id, StatusType statusType);

    BalanceResponseDto toBalance(Long id, BalanceRequestDto balanceRequestDto);

    BalanceResponseDto fromBalance(Long id, BalanceRequestDto balanceRequestDto);
}
