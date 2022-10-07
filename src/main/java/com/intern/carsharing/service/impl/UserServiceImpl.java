package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.BalanceResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
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
    public BalanceResponseDto toBalance(Long id, BalanceRequestDto balanceRequestDto) {
        permissionService.check(id);
        Balance balance = balanceService.findByUserId(id);
        balance.setValue(balance.getValue().add(balanceRequestDto.getValue()));
        balanceService.save(balance);
        String message = "Balance was credited successfully.";
        return getBalanceSuccessResponse(
                id, message, balanceRequestDto.getValue(), balance.getCurrency());
    }

    private BalanceResponseDto getBalanceSuccessResponse(
            long userId, String message, BigDecimal value, String currency) {
        BalanceResponseDto responseDto = new BalanceResponseDto();
        responseDto.setUserId(userId);
        responseDto.setMessage(message);
        responseDto.setValue(value);
        responseDto.setCurrency(currency);
        return responseDto;
    }

    @Override
    @Transactional
    public BalanceResponseDto fromBalance(Long id, BalanceRequestDto balanceRequestDto) {
        Balance balance = balanceService.findByUserId(id);
        BigDecimal currentValue = balance.getValue();
        BigDecimal requestValue = balanceRequestDto.getValue();
        if (currentValue.compareTo(requestValue) < 0) {
            String message = "Not enough money for a transaction";
            return getBalanceSuccessResponse(id, message, requestValue, balance.getCurrency());
        }
        balance.setValue(currentValue.subtract(requestValue));
        balanceService.save(balance);
        String message = "Balance was debited successfully.";
        return getBalanceSuccessResponse(id, message, requestValue, balance.getCurrency());
    }
}
