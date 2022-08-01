package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.RoleService;
import com.intern.carsharing.service.StatusService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper implements RequestDtoMapper<User, RegistrationRequestUserDto>,
        ResponseDtoMapper<User, ResponseUserDto> {
    private static final String STATUS_ENABLE = "ENABLE";
    private final ModelMapper mapper;
    private final StatusService statusService;
    private final RoleService roleService;

    @Override
    public User toModel(RegistrationRequestUserDto dto) {
        User user = mapper.map(dto, User.class);
        Set<Role> roles = dto.getRoles().stream()
                .map(role -> roleService.findByName(Role.RoleName.valueOf(role)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        user.setStatus(statusService.findByStatusType(Status.StatusType.valueOf(STATUS_ENABLE)));
        return user;
    }

    @Override
    public ResponseUserDto toDto(User model) {
        ResponseUserDto responseUserDto = mapper.map(model, ResponseUserDto.class);
        Set<String> roles = model.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toSet());
        responseUserDto.setRoles(roles);
        responseUserDto.setStatus(model.getStatus().getStatusType().name());
        return responseUserDto;
    }
}
