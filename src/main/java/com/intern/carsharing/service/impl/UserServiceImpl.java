package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ApiExceptionObject;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private static final String BACKOFFICE_SERVICE_HOST = "http://localhost:8082";
    private static final String CAR_SERVICE_HOST = "http://localhost:8084";
    private final UserRepository userRepository;
    private final StatusService statusService;
    private final BalanceService balanceService;
    private final PermissionService permissionService;

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
        WebClient client = WebClient.create(BACKOFFICE_SERVICE_HOST);
        return client
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
    public String getCarStatistics(Long userId, Long carId) {
        permissionService.check(userId);
        return "Your car statistics";
    }

    @Override
    public ResponseEntity<Object> addCarToRent(Long userId, CarRegistrationRequestDto requestDto) {
        permissionService.check(userId);
        requestDto.setCarOwnerId(userId);
        try {
            return executeRequest(requestDto);
        } catch (WebClientResponseException e) {
            ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                    e.getMessage(), e.getStatusCode(), LocalDateTime.now().toString()
            );
            return new ResponseEntity<>(apiExceptionObject, e.getStatusCode());
        }
    }

    private ResponseEntity<Object> executeRequest(CarRegistrationRequestDto requestDto) {
        WebClient client = WebClient.create(CAR_SERVICE_HOST);
        Object response = client
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
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public String changeCarStatus(Long userId, Long carId, ChangeCarStatusRequestDto requestDto) {
        permissionService.check(userId);
        return "Your car status was changed";
    }
}
