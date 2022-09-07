package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.StatisticsResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    public List<StatisticsResponseDto> getTripStatistics(
            Long userId, LocalDate startDate, LocalDate endDate
    ) {
        return List.of(new StatisticsResponseDto());
    }
}
