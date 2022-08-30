package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.LimitedPermissionException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StatusService statusService;
    private final BalanceService balanceService;

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
        checkPermission(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with id: " + id));
    }

    private void checkPermission(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getPrincipal().toString().equals("anonymousUser")) {
            User client = (User) authentication.getDetails();
            if (!Objects.equals(client.getId(), id) && !containsRoleAdmin(client.getRoles())) {
                throw new LimitedPermissionException(
                        "Users have access only to their own accounts."
                );
            }
        }
    }

    private boolean containsRoleAdmin(Set<Role> roles) {
        return roles.stream()
                .anyMatch(role -> role.getRoleName().name().equals("ADMIN"));
    }

    @Override
    @Transactional
    public User update(Long id, UserUpdateRequestDto updateDto) {
        User user = get(id);
        String newEmail = updateDto.getEmail();
        User userWithSameNewEmail = findByEmail(newEmail);
        if (userWithSameNewEmail != null && !userWithSameNewEmail.getId().equals(id)) {
            throw new UserAlreadyExistException("User with email " + newEmail
                    + " already exists");
        }
        setUpdates(user, updateDto);
        return userRepository.save(user);
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
    public String toBalance(Long id, BalanceRequestDto balanceRequestDto) {
        checkPermission(id);
        Balance balance = balanceService.findByUserId(id);
        balance.setValue(balance.getValue().add(balanceRequestDto.getValue()));
        balanceService.save(balance);
        return balanceRequestDto.getValue() + " " + balance.getCurrency()
            + " has been credited to the balance of the user with id " + id;
    }

}
