package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.service.RoleService;
import com.intern.carsharing.service.StatusService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    private static final String STATUS_ENABLE = "ENABLE";
    @Autowired
    protected RoleService roleService;
    @Autowired
    protected StatusService statusService;

    @AfterMapping
    protected void addStatusEnable(@MappingTarget User user) {
        user.setStatus(statusService.findByStatusType(StatusType.valueOf(STATUS_ENABLE)));
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByName(RoleName.USER));
        user.setRoles(roles);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    public abstract User toModel(RegistrationRequestUserDto dto);

    public abstract ResponseUserDto toDto(User user);

    public Set<String> toStrings(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }

    public String toStringStatus(Status status) {
        return status.getStatusType().name();
    }
}
