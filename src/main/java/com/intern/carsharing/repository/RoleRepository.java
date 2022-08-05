package com.intern.carsharing.repository;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.util.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(RoleName roleName);
}
