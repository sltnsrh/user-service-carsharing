package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import org.springframework.http.ResponseEntity;

public interface CarClientService {
    ResponseEntity<Object> getCarStatistics(
            Long userId, Long carId, String startDate, String endDate,
            String carType, String bearerToken
    );

    ResponseEntity<Object> addCarToRent(Long userId, CarRegistrationRequestDto requestDto,
                                        String bearerToken);

    ResponseEntity<Object> changeCarStatus(
            Long userId, Long carId, ChangeCarStatusRequestDto requestDto, String bearerToken
    );
}
