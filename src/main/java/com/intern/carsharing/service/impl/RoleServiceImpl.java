package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.repository.RoleRepository;
import com.intern.carsharing.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role findByName(Role.RoleName roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
