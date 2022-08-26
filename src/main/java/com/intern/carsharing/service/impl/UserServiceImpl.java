package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.LimitedPermissionException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with id: " + id));
    }

    @Override
    @Transactional
    public User update(Long id, UserUpdateRequestDto updateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User client = (User) authentication.getDetails();
        if (!Objects.equals(client.getId(), id) && !containsRoleAdmin(client.getRoles())) {
            throw new LimitedPermissionException("Users can update only own accounts.");
        }
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

    @Override
    @Transactional
    public User changeStatus(Long id, StatusType statusType) {
        User user = get(id);
        user.setStatus(statusService.findByStatusType(statusType));
        return save(user);
    }

    private void setUpdates(User user, UserUpdateRequestDto updateDto) {
        user.setEmail(updateDto.getEmail());
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setAge(updateDto.getAge());
        user.setDriverLicence(updateDto.getDriverLicence());
    }

    private boolean containsRoleAdmin(Set<Role> roles) {
        return roles.stream()
                .anyMatch(role -> role.getRoleName().name().equals("ADMIN"));
    }
}
