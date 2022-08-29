package com.intern.carsharing.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.RefreshTokenException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.RefreshTokenService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    void registerWithValidData() {
        RegistrationUserRequestDto requestUserDto = new RegistrationUserRequestDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        user.setStatus(new Status(1L, StatusType.valueOf("INVALIDATE")));

        User userAfterSave = new User();
        userAfterSave.setId(1L);
        userAfterSave.setEmail("bob@gmail.com");
        userAfterSave.setPassword("password");
        userAfterSave.setFirstName("Bob");
        userAfterSave.setLastName("Alister");
        userAfterSave.setAge(21);
        userAfterSave.setDriverLicence("DFG23K34H");
        userAfterSave.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        userAfterSave.setStatus(new Status(1L, StatusType.valueOf("INVALIDATE")));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("token");
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));

        Mockito.when(userService.save(user)).thenReturn(userAfterSave);
        Mockito.when(userMapper.toModel(requestUserDto)).thenReturn(user);
        Mockito.when(passwordEncoder.encode("password"))
                .thenReturn("$2a$10$hTlj76.onzhNMv/sh64KZ.NQl30XxR7lhbOIeAeP8hO7d6UTJyo/C");
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);
        Mockito.when(confirmationTokenService.create(userAfterSave)).thenReturn(confirmationToken);

        String actual = authService.register(requestUserDto);
        Assertions.assertFalse(actual.isBlank());
        Assertions.assertTrue(actual.contains("confirm?token=token"));
    }

    @Test
    void loginWithValidData() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Mockito.when(jwtTokenProvider.createToken("bob@gmail.com", user.getRoles()))
                .thenReturn("token");
        Mockito.when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "bob@gmail.com", "password"))).thenReturn(null);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refreshtoken");
        Mockito.when(refreshTokenService.create(user.getId())).thenReturn(refreshToken);

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");

        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        Assertions.assertEquals("bob@gmail.com", loginResponseDto.getEmail());
        Assertions.assertEquals("token", loginResponseDto.getToken());
        Assertions.assertEquals("refreshtoken", loginResponseDto.getRefreshToken());

    }

    @Test
    void confirmWithValidConfirmationToken() {
        User user = new User();
        user.setEmail("bob@gmail.com");

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("confirmationToken");
        confirmationToken.setId(1L);
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        confirmationToken.setUser(user);
        Mockito.when(confirmationTokenService
                .findByToken("confirmationToken")).thenReturn(confirmationToken);
        String actual = authService.confirm("confirmationToken");
        Assertions.assertEquals(
                actual, "Your email address: bob@gmail.com was confirmed successfully!"
        );
    }

    @Test
    void confirmWithNotExistToken() {
        Mockito.when(confirmationTokenService
                .findByToken("confirmationToken")).thenReturn(null);
        assertThrows(ConfirmationTokenInvalidException.class,
                () -> authService.confirm("confirmationToken"));
    }

    @Test
    void confirmAlreadyConfirmedUser() {
        User user = new User();
        user.setEmail("bob@gmail.com");

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("confirmationToken");
        confirmationToken.setId(1L);
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        confirmationToken.setUser(user);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        Mockito.when(confirmationTokenService.findByToken("confirmationToken"))
                .thenReturn(confirmationToken);
        assertThrows(ConfirmationTokenInvalidException.class,
                () -> authService.confirm("confirmationToken"));
    }

    @Test
    void confirmWithExpiredToken() {
        User user = new User();
        user.setEmail("bob@gmail.com");

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("confirmationToken");
        confirmationToken.setId(1L);
        confirmationToken.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        confirmationToken.setExpiredAt(LocalDateTime.now());
        confirmationToken.setUser(user);
        Mockito.when(confirmationTokenService.findByToken("confirmationToken"))
                .thenReturn(confirmationToken);
        String actual = authService.confirm("confirmationToken");
        Assertions.assertTrue(actual.contains("Confirmation token was expired"));
    }

    @Test
    void resendEmailWithExistAndInvalidateEmail() {
        User user = new User();
        user.setStatus(new Status(1L, StatusType.INVALIDATE));

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken("confirmationToken");
        confirmationToken.setId(1L);
        confirmationToken.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        confirmationToken.setExpiredAt(LocalDateTime.now());
        confirmationToken.setUser(user);
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        Mockito.when(confirmationTokenService.create(user)).thenReturn(confirmationToken);
        String actual = authService.resendEmail("bob@gmail.com");
        Assertions.assertTrue(actual.contains("Thanks for the registration"));
    }

    @Test
    void resendEmailWithNotExistUser() {
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> authService.resendEmail("bob@gmail.com"));
    }

    @Test
    void resendEmailWithValidatedUser() {
        User user = new User();
        user.setStatus(new Status(1L, StatusType.ACTIVE));

        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        assertThrows(ConfirmationTokenInvalidException.class,
                () -> authService.resendEmail("bob@gmail.com"));
    }

    @Test
    void refreshAuthTokenWithValidRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        refreshToken.setToken("refreshtoken");

        Mockito.when(refreshTokenService.getByToken("refreshtoken")).thenReturn(refreshToken);
        Mockito.when(jwtTokenProvider.createToken(user.getEmail(), user.getRoles())).thenReturn("newauthtoken");
        LoginResponseDto actual = authService.refreshToken(requestDto);
        Assertions.assertEquals(user.getEmail(), actual.getEmail());
        Assertions.assertEquals("newauthtoken", actual.getToken());
        Assertions.assertEquals("refreshtoken", actual.getRefreshToken());
    }

    @Test
    void refreshAuthTokenWithExpiredRefreshToken() {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(new User());
        refreshToken.setExpiredAt(LocalDateTime.now().minusMinutes(15));

        Mockito.when(refreshTokenService.getByToken("refreshtoken")).thenReturn(refreshToken);
        Assertions.assertThrows(RefreshTokenException.class, () -> authService.refreshToken(requestDto));
    }
}
