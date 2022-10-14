package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.service.BackofficeClientService;
import com.intern.carsharing.service.PermissionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BackofficeClientServiceImpl extends ClientService implements BackofficeClientService {
    private final WebClient backofficeClient;
    private final PermissionService permissionService;

    @Override
    public Object getTripStatistics(
            Long userId, String startDate, String endDate, String carType, String bearerToken
    ) {
        permissionService.check(userId);
        return backofficeClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/orders/" + userId)
                        .queryParams(getPresentQueryParams(startDate, endDate, carType))
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @Override
    public List<OrderDto> getAllCarOrders(
            MultiValueMap<String, String> queryParams, Long carId, String bearerToken) {
        OrderDto[] orderDtoArray = backofficeClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/manager/orders")
                        .queryParams(queryParams)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .block();
        if (orderDtoArray != null) {
            return Arrays.stream(orderDtoArray)
                    .filter(order -> order.getCarId().equals(carId))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
