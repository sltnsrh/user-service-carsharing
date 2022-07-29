package com.intern.carsharing.service;

import com.intern.carsharing.model.Role;

public interface RoleService {
    Role findByName(Role.RoleName roleName);
}
