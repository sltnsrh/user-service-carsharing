package com.intern.carsharing.service;

import com.intern.carsharing.exception.LimitedPermissionException;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ANONYMOUS_USER = "anonymousUser";

    public void check(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getPrincipal().toString().equals(ANONYMOUS_USER)) {
            User client = (User) authentication.getDetails();
            checkClientIdMatchesRequestedId(client, id);
        }
    }

    private void checkClientIdMatchesRequestedId(User client, Long id) {
        if (!Objects.equals(client.getId(), id) && !containsRoleAdmin(client.getRoles())) {
            throw new LimitedPermissionException(
                    "Users have access only to their own accounts."
            );
        }
    }

    private boolean containsRoleAdmin(Set<Role> roles) {
        return roles.stream()
                .anyMatch(role -> role.getRoleName().name().equals(ADMIN_ROLE));
    }
}
