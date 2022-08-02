package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.RoleService;
import com.intern.carsharing.service.StatusService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    @InjectMocks
    private UserMapper userMapper;
    @Mock
    private RoleService roleService;
    @Mock
    private StatusService statusService;
    @Mock
    private ModelMapper mapper;

    @Test
    void toModel_validData_ok() {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));
        Role roleAdmin = new Role(1L, Role.RoleName.ADMIN);
        Status statusEnable = new Status(1L, Status.StatusType.ENABLE);

        User userFromModelMapper = new User();
        userFromModelMapper.setEmail("bob@gmail.com");
        userFromModelMapper.setPassword("password");
        userFromModelMapper.setFirstName("Bob");
        userFromModelMapper.setLastName("Alister");
        userFromModelMapper.setAge(21);
        userFromModelMapper.setDriverLicence("DFG23K34H");
        userFromModelMapper.setRoles(Set.of(new Role(1L, Role.RoleName.valueOf("ADMIN"))));
        userFromModelMapper.setStatus(new Status(1L, Status.StatusType.valueOf("ENABLE")));

        Mockito.when(mapper.map(requestUserDto, User.class)).thenReturn(userFromModelMapper);
        Mockito.when(roleService.findByName(Role.RoleName.valueOf("ADMIN"))).thenReturn(roleAdmin);
        Mockito.when(statusService.findByStatusType(Status.StatusType.valueOf("ENABLE"))).thenReturn(statusEnable);

        User actual = userMapper.toModel(requestUserDto);
        Assertions.assertNull(actual.getId());
        Assertions.assertEquals("bob@gmail.com", actual.getEmail());
        Assertions.assertEquals("password", actual.getPassword());
        Assertions.assertEquals("Bob", actual.getFirstName());
        Assertions.assertEquals("Alister", actual.getLastName());
        Assertions.assertEquals(21, actual.getAge());
        Assertions.assertEquals("DFG23K34H", actual.getDriverLicence());
        Assertions.assertEquals(Set.of(roleAdmin), actual.getRoles());
        Assertions.assertEquals(statusEnable, actual.getStatus());
    }

    @Test
    void toDto_validData_ok() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, Role.RoleName.valueOf("ADMIN"))));
        user.setStatus(new Status(1L, Status.StatusType.valueOf("ENABLE")));

        ResponseUserDto responseUserDtoFromMapper = new ResponseUserDto();
        responseUserDtoFromMapper.setId(1L);
        responseUserDtoFromMapper.setEmail("bob@gmail.com");
        responseUserDtoFromMapper.setPassword("password");
        responseUserDtoFromMapper.setFirstName("Bob");
        responseUserDtoFromMapper.setLastName("Alister");
        responseUserDtoFromMapper.setAge(21);
        responseUserDtoFromMapper.setDriverLicence("DFG23K34H");
        responseUserDtoFromMapper.setRoles((new HashSet<>(Collections
                .singleton(String.valueOf(new Role(1L, Role.RoleName.ADMIN))))));
        responseUserDtoFromMapper.setStatus(String
                .valueOf(new Status(1L, Status.StatusType.valueOf("ENABLE"))));
        Mockito.when(mapper.map(user, ResponseUserDto.class)).thenReturn(responseUserDtoFromMapper);

        ResponseUserDto actual = userMapper.toDto(user);
        Assertions.assertEquals(1L, actual.getId());
        Assertions.assertEquals("bob@gmail.com", actual.getEmail());
        Assertions.assertEquals("password", actual.getPassword());
        Assertions.assertEquals("Bob", actual.getFirstName());
        Assertions.assertEquals("Alister", actual.getLastName());
        Assertions.assertEquals(21, actual.getAge());
        Assertions.assertEquals("DFG23K34H", actual.getDriverLicence());
        Assertions.assertEquals(Set.of("ADMIN"), actual.getRoles());
        Assertions.assertEquals("ENABLE", actual.getStatus());
    }
}