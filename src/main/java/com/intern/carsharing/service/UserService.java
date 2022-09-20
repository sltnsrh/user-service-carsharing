package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.util.StatusType;
import org.springframework.http.ResponseEntity;

public interface UserService {
    User findByEmail(String email);

    User save(User user);

    User get(Long id);

    User update(Long id, UserUpdateRequestDto updateDto);

    User changeStatus(Long id, StatusType statusType);

    String toBalance(Long id, BalanceRequestDto balanceRequestDto);

    String fromBalance(Long id, BalanceRequestDto balanceRequestDto);

    Object getTripStatistics(
            Long userId, String startDate, String endDate, String carType
    );

    String getCarStatistics(Long userId, Long carId);

    ResponseEntity<Object> addCarToRent(Long userId, CarRegistrationRequestDto requestDto);

    ResponseEntity<Object> changeCarStatus(
            Long userId, Long carId, ChangeCarStatusRequestDto requestDto
    );
}
