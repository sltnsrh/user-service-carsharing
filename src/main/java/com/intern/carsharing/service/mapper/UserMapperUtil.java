package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.service.RoleService;
import com.intern.carsharing.service.StatusService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperUtil {
    private final RoleService roleService;
    private final StatusService statusService;

    @Named("setStatusActive")
    Status setStatusActive(String value) {
        return statusService.findByStatusType(StatusType.INVALIDATE);
    }

    @Named("setUserRole")
    Set<Role> setUserRole(@NotNull String value) {
        String roleName = value.toUpperCase();
        if (roleName.equals("USER") || roleName.equals("CAR_OWNER")) {
            Set<Role> roles = new HashSet<>();
            roles.add(roleService.findByName(RoleName.valueOf(roleName)));
            return roles;
        }
        throw new IllegalArgumentException("There is no role: " + roleName
                + ". You can choose between USER and CAR_OWNER");
    }

    @Named("rolesToSetString")
    Set<String> rolesToSetString(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }

    @Named("statusToString")
    String statusToString(Status status) {
        return status.getStatusType().name();
    }
}
