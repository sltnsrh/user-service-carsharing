package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.exception.UserNotFoundException;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RequestUserUpdateDto;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.UserService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
    public User update(Long id, RequestUserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with id: " + id));
        String newEmail = updateDto.getEmail();
        if (findByEmail(newEmail) != null && !Objects.equals(newEmail, user.getEmail())) {
            throw new UserAlreadyExistException("User with email " + newEmail
                    + " is already exist");
        }
        setUpdates(user, updateDto);
        return userRepository.save(user);
    }

    private void setUpdates(User user, RequestUserUpdateDto updateDto) {
        user.setEmail(updateDto.getEmail());
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setAge(updateDto.getAge());
        user.setDriverLicence(updateDto.getDriverLicence());
    }
}
