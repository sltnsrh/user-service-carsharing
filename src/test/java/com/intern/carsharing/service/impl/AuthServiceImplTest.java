package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

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
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        user.setStatus(new Status(1L, StatusType.valueOf("ACTIVE")));
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
        userAfterSave.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        userAfterSave.setStatus(new Status(1L, StatusType.valueOf("ACTIVE")));
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
        responseUserDto.setStatus("ACTIVE");
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
        Assertions.assertEquals("ACTIVE", actual.getStatus());
    }

    @Test
    void login_validData_ok() {

        User user = new User();
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Mockito.when(jwtTokenProvider.createToken("bob@gmail.com", user.getRoles()))
                .thenReturn("token");
        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "bob@gmail.com", "password"))).thenReturn(null);

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");

        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals(loginResponseDto.getEmail(), "bob@gmail.com");
        Assertions.assertEquals(loginResponseDto.getToken(), "token");
    }
}
