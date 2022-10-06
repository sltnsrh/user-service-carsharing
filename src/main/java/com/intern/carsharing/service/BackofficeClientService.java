package com.intern.carsharing.service;

import com.intern.carsharing.model.dto.response.OrderDto;
import java.util.List;
import org.springframework.util.MultiValueMap;

public interface BackofficeClientService {
    Object getTripStatistics(
            Long userId, String startDate, String endDate, String carType
    );

    List<OrderDto> getAllCarOrders(
            MultiValueMap<String, String> queryParams, Long carId);
}
