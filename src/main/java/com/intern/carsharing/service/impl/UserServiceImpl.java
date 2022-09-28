package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ApiExceptionObject;
import com.intern.carsharing.exception.CarIsRentedException;
import com.intern.carsharing.exception.CarNotFoundException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.CarDto;
import com.intern.carsharing.model.dto.response.CarStatisticsResponseDto;
import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.CarMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String RENTED_STATUS = "RENTED";
    private final WebClient backofficeServiceClient;
    private final WebClient carServiceClient;
    private final UserRepository userRepository;
    private final StatusService statusService;
    private final BalanceService balanceService;
    private final PermissionService permissionService;
    private final CarMapper carMapper;

    @Override
    public User findByEmail(String email) {
        return userRepository.findUserByEmail(email).orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User get(Long id) {
        permissionService.check(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with id: " + id));
    }

    @Override
    @Transactional
    public User update(Long id, UserUpdateRequestDto updateDto) {
        User user = get(id);
        checkIfUserWithNewEmailExists(id, updateDto);
        setUpdates(user, updateDto);
        return userRepository.save(user);
    }

    private void checkIfUserWithNewEmailExists(Long id, UserUpdateRequestDto updateDto) {
        String newEmail = updateDto.getEmail();
        User userWithSameNewEmail = findByEmail(newEmail);
        if (userWithSameNewEmail != null && !userWithSameNewEmail.getId().equals(id)) {
            throw new UserAlreadyExistException("User with email " + newEmail
                    + " already exists");
        }
    }

    private void setUpdates(User user, UserUpdateRequestDto updateDto) {
        user.setEmail(updateDto.getEmail());
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setAge(updateDto.getAge());
        user.setDriverLicence(updateDto.getDriverLicence());
    }

    @Override
    @Transactional
    public User changeStatus(Long id, StatusType statusType) {
        User user = get(id);
        user.setStatus(statusService.findByStatusType(statusType));
        return save(user);
    }

    @Override
    @Transactional
    public String toBalance(Long id, BalanceRequestDto balanceRequestDto) {
        permissionService.check(id);
        Balance balance = balanceService.findByUserId(id);
        balance.setValue(balance.getValue().add(balanceRequestDto.getValue()));
        balanceService.save(balance);
        return balanceRequestDto.getValue() + " " + balance.getCurrency()
            + " has been credited to the balance of the user with id " + id;
    }

    @Override
    @Transactional
    public String fromBalance(Long id, BalanceRequestDto balanceRequestDto) {
        Balance balance = balanceService.findByUserId(id);
        BigDecimal currentValue = balance.getValue();
        BigDecimal requestValue = balanceRequestDto.getValue();
        if (currentValue.compareTo(requestValue) < 0) {
            return "Not enough money on balance for a transaction";
        }
        balance.setValue(currentValue.subtract(requestValue));
        balanceService.save(balance);
        return requestValue + " " + balance.getCurrency()
                + " were debited from the balance of the user with id " + id;
    }

    @Override
    public Object getTripStatistics(
            Long userId, String startDate, String endDate, String carType
    ) {
        permissionService.check(userId);
        return backofficeServiceClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/orders/" + userId)
                        .queryParams(getPresentQueryParams(startDate, endDate, carType))
                        .build()
                )
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    private MultiValueMap<String, String> getPresentQueryParams(
            String startDate, String endDate, String carType
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (carType != null) {
            queryParams.add("carType", carType);
        }
        if (startDate != null) {
            queryParams.add("dateStart", startDate);
        }
        if (endDate != null) {
            queryParams.add("dateEnd", endDate);
        }
        return queryParams;
    }

    @Override
    public ResponseEntity<Object> getCarStatistics(
            Long userId, Long carId, String startDate, String endDate, String carType
    ) {
        permissionService.check(userId);
        CarDto car = getCarById(carId);
        checkIfCarBelongsUser(car, userId, carId);
        MultiValueMap<String, String> queryParams =
                getPresentQueryParams(startDate, endDate, carType);
        List<OrderDto> carOrders = getAllCarOrders(queryParams, carId);
        CarStatisticsResponseDto responseDto = carMapper.toStatisticsDto(car);
        responseDto.setOrders(carOrders);
        responseDto.setTripsNumber(carOrders.size());
        BigDecimal generalIncome = carOrders.stream()
                .map(OrderDto::getPrice)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        responseDto.setGeneralIncome(generalIncome);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    private CarDto getCarById(Long carId) {
        try {
            return carServiceClient
                    .get()
                    .uri("/cars/" + carId)
                    .retrieve()
                    .bodyToMono(CarDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new CarNotFoundException("Can't find car with id: " + carId);
        }
    }

    private List<OrderDto> getAllCarOrders(MultiValueMap<String, String> queryParams, Long carId) {
        OrderDto[] orderDtoArray = backofficeServiceClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/manager/orders")
                        .queryParams(queryParams)
                        .build()
                )
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

    @Override
    public ResponseEntity<Object> addCarToRent(Long userId, CarRegistrationRequestDto requestDto) {
        permissionService.check(userId);
        requestDto.setCarOwnerId(userId);
        try {
            return executeRequest(requestDto);
        } catch (WebClientResponseException e) {
            return createResponseEntityException(e);
        }
    }

    private ResponseEntity<Object> executeRequest(CarRegistrationRequestDto requestDto) {
        Object response = carServiceClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cars")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private ResponseEntity<Object> createResponseEntityException(
            WebClientResponseException e
    ) {
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(), e.getStatusCode(), LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, e.getStatusCode());
    }

    @Override
    public ResponseEntity<Object> changeCarStatus(
            Long userId, Long carId, ChangeCarStatusRequestDto requestDto
    ) {
        permissionService.check(userId);
        CarDto car = getCarById(carId);
        checkIfCarBelongsUser(car, userId, carId);
        checkIfCarNotRented(car);
        try {
            Object response = carServiceClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cars/status/" + carId)
                            .queryParam("carStatus", requestDto.getStatus())
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (WebClientResponseException e) {
            return createResponseEntityException(e);
        }
    }

    private void checkIfCarBelongsUser(CarDto car, Long userId, Long carId) {
        if (!Objects.equals(car.getCarOwnerId(), userId)) {
            throw new CarNotFoundException("Car with id: " + carId
                    + " doesn't belong to the user with id: " + userId);
        }
    }

    private void checkIfCarNotRented(CarDto car) {
        if (car.getCarStatus().equals(RENTED_STATUS)) {
            throw new CarIsRentedException("Your car is rented now, "
                    + "you can't change the status");
        }
    }
}
