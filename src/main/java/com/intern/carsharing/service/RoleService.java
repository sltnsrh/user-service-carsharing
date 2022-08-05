package com.intern.carsharing.service;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.util.RoleName;

public interface RoleService {
    Role findByName(RoleName roleName);
}
