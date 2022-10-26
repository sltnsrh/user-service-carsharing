package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.service.BackofficeClientService;
import com.intern.carsharing.service.DiscoveryUrlService;
import com.intern.carsharing.service.PermissionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Log4j2
@RequiredArgsConstructor
public class BackofficeClientServiceImpl extends ClientService implements BackofficeClientService {
    private final DiscoveryUrlService urlService;
    private final PermissionService permissionService;

    @Override
    public Object getTripStatistics(
            Long userId, String startDate, String endDate, String carType, String bearerToken
    ) {
        permissionService.check(userId);
        log.info("Sending request to: "
                + urlService.getBackofficeServiceUrl()
                + "user/orders/" + userId);
        return WebClient.builder().baseUrl(urlService.getBackofficeServiceUrl()).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("user/orders/" + userId)
                        .queryParams(getPresentQueryParams(startDate, endDate, carType))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    @Override
    public List<OrderDto> getAllCarOrders(
            MultiValueMap<String, String> queryParams, Long carId, String bearerToken) {
        log.info("Sending request to: "
                + urlService.getBackofficeServiceUrl()
                + String.format("user/cars/%s/orders", carId));
        OrderDto[] orderDtoArray =
                WebClient.builder().baseUrl(urlService.getBackofficeServiceUrl()).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(String.format("user/cars/%s/orders", carId))
                        .queryParams(queryParams)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .block();
        if (orderDtoArray != null) {
            return Arrays.stream(orderDtoArray)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
