package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void register_validData_ok() {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, Role.RoleName.valueOf("ADMIN"))));
        user.setStatus(new Status(1L, Status.StatusType.valueOf("ENABLE")));
        Mockito.when(userMapper.toModel(requestUserDto)).thenReturn(user);
        Mockito.when(passwordEncoder.encode("password"))
                .thenReturn("$2a$10$hTlj76.onzhNMv/sh64KZ.NQl30XxR7lhbOIeAeP8hO7d6UTJyo/C");

        User userAfterSave = new User();
        userAfterSave.setId(1L);
        userAfterSave.setEmail("bob@gmail.com");
        userAfterSave.setPassword("password");
        userAfterSave.setFirstName("Bob");
        userAfterSave.setLastName("Alister");
        userAfterSave.setAge(21);
        userAfterSave.setDriverLicence("DFG23K34H");
        userAfterSave.setRoles(Set.of(new Role(1L, Role.RoleName.valueOf("ADMIN"))));
        userAfterSave.setStatus(new Status(1L, Status.StatusType.valueOf("ENABLE")));
        Mockito.when(userService.save(user)).thenReturn(userAfterSave);

        ResponseUserDto responseUserDto = new ResponseUserDto();
        responseUserDto.setId(1L);
        responseUserDto.setEmail("bob@gmail.com");
        responseUserDto.setPassword("password");
        responseUserDto.setFirstName("Bob");
        responseUserDto.setLastName("Alister");
        responseUserDto.setAge(21);
        responseUserDto.setDriverLicence("DFG23K34H");
        responseUserDto.setRoles(Set.of("ADMIN"));
        responseUserDto.setStatus("ENABLE");
        Mockito.when(userMapper.toDto(userAfterSave)).thenReturn(responseUserDto);

        ResponseUserDto actual = authService.register(requestUserDto);
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