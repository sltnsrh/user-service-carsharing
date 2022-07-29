package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.AuthService;
import com.intern.carsharing.service.RoleService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String STATUS_ENABLE = "ENABLE";
    private final UserService userService;
    private final UserMapper mapper;
    private final StatusService statusService;
    private final RoleService roleService;

    @Override
    public ResponseUserDto register(RegistrationRequestUserDto requestUserDto) {
        String email = requestUserDto.getEmail();
        if (userExist(email)) {
            throw new UserAlreadyExistException("User with email " + email + " is already exist");
        }
        User user = mapper.toModel(requestUserDto);
        Set<Role> roles = requestUserDto.getRoles().stream()
                .map(role -> roleService.findByName(Role.RoleName.valueOf(role)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        user.setStatus(statusService.findByStatusType(Status.StatusType.valueOf(STATUS_ENABLE)));
        return mapper.toDto(userService.save(user));
    }

    private boolean userExist(String email) {
        User user = userService.findByEmail(email);
        return user != null;
    }
}
