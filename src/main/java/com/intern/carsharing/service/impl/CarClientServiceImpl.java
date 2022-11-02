package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.CarIsRentedException;
import com.intern.carsharing.exception.CarNotFoundException;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.response.CarDto;
import com.intern.carsharing.model.dto.response.CarStatisticsResponseDto;
import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.service.CarClientService;
import com.intern.carsharing.service.DiscoveryUrlService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.mapper.CarMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Log4j2
@RequiredArgsConstructor
public class CarClientServiceImpl extends ClientService implements CarClientService {
    private static final String RENTED_STATUS = "RENTED";
    private final CarMapper carMapper;
    private final PermissionService permissionService;
    private final BackofficeClientServiceImpl backofficeClientService;
    private final DiscoveryUrlService urlService;

    @Override
    public ResponseEntity<Object> getCarStatistics(
            Long userId, Long carId, String startDate, String endDate, String carType,
            String bearerToken
    ) {
        permissionService.check(userId);
        CarDto car = getCarById(carId, bearerToken);
        checkIfCarBelongsUser(car, userId, carId);
        MultiValueMap<String, String> queryParams =
                getPresentQueryParams(startDate, endDate, carType);
        List<OrderDto> carOrders =
                backofficeClientService.getAllCarOrders(queryParams, carId, bearerToken);
        CarStatisticsResponseDto responseDto = carMapper.toStatisticsDto(car);
        responseDto.setOrders(carOrders);
        responseDto.setTripsNumber(carOrders.size());
        BigDecimal generalIncome = carOrders.stream()
                .map(OrderDto::getPrice)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        responseDto.setGeneralIncome(generalIncome);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    private CarDto getCarById(Long carId, String bearerToken) {
        try {
            log.info("Sending request to: "
                    + urlService.getCarServiceUrl() + "cars/" + carId);
            return WebClient.builder().baseUrl(urlService.getCarServiceUrl()).build()
                    .get()
                    .uri("cars/" + carId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(CarDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new CarNotFoundException("Can't find car with id: " + carId);
        }
    }

    @Override
    public ResponseEntity<Object> addCarToRent(
            Long userId, CarRegistrationRequestDto requestDto, String bearerToken) {
        permissionService.check(userId);
        requestDto.setOwnerId(userId);
        try {
            return executeRequest(requestDto, bearerToken);
        } catch (WebClientResponseException e) {
            return createResponseEntityException(e);
        }
    }

    private ResponseEntity<Object> executeRequest(CarRegistrationRequestDto requestDto,
                                                  String bearerToken) {
        log.info("Sending request to: "
                + urlService.getCarServiceUrl() + "cars");
        Object response = WebClient.builder().baseUrl(urlService.getCarServiceUrl()).build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("cars")
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> changeCarStatus(
            Long userId, Long carId, ChangeCarStatusRequestDto requestDto, String bearerToken
    ) {
        permissionService.check(userId);
        CarDto car = getCarById(carId, bearerToken);
        checkIfCarBelongsUser(car, userId, carId);
        checkIfCarNotRented(car);
        try {
            log.info("Sending request to: "
                    + urlService.getCarServiceUrl() + "cars/change-car-status");
            requestDto.setCarId(carId);
            Object response = WebClient.builder().baseUrl(urlService.getCarServiceUrl()).build()
                    .patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("cars/change-car-status")
                            .build()
                    )
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestDto))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (WebClientResponseException e) {
            return createResponseEntityException(e);
        }
    }

    private void checkIfCarBelongsUser(CarDto car, long userId, long carId) {
        if (!Objects.equals(car.getOwnerId(), userId)) {
            throw new CarNotFoundException("Car with id: " + carId
                    + " doesn't belong to the user with id: " + userId);
        }
    }

    private void checkIfCarNotRented(CarDto car) {
        if (car.getStatus().equals(RENTED_STATUS)) {
            throw new CarIsRentedException("Your car is rented now, "
                    + "you can't change the status");
        }
    }
}
