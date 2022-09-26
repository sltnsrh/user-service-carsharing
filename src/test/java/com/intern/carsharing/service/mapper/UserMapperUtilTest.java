package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.service.RoleService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMapperUtilTest {
    @InjectMocks
    private UserMapperUtil userMapperUtil;
    @Mock
    private RoleService roleService;

    @Test
    void setUserRoleWithInvalidRole() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userMapperUtil.setUserRole("INVALIDROLE"));
    }

    @Test
    void setUserRoleWithRoleUser() {
        Role userRole = new Role(1L, RoleName.USER);
        Mockito.when(roleService.findByName(userRole.getRoleName())).thenReturn(userRole);
        Set<Role> actual = userMapperUtil.setUserRole("USER");
        Assertions.assertEquals(Set.of(userRole), actual);
    }

    @Test
    void setUserRoleWithRoleCarOwner() {
        Role carOwnerRole = new Role(1L, RoleName.CAR_OWNER);
        Mockito.when(roleService.findByName(carOwnerRole.getRoleName())).thenReturn(carOwnerRole);
        Set<Role> actual = userMapperUtil.setUserRole("CAR_OWNER");
        Assertions.assertEquals(Set.of(carOwnerRole), actual);
    }
}
