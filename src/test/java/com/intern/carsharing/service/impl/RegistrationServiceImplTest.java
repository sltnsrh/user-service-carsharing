package com.intern.carsharing.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.DriverLicenceAlreadyExistException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.EmailConfirmationResponseDto;
import com.intern.carsharing.model.dto.response.RegistrationResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {
    @InjectMocks
    private RegistrationServiceImpl registrationService;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private BalanceService balanceService;
    @Spy
    private AuthResponseBuilder authResponseBuilder;

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
        requestUserDto.setRole("USER");

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, RoleName.valueOf("USER"))));
        user.setStatus(new Status(1L, StatusType.valueOf("INVALIDATE")));

        User userAfterSave = new User();
        userAfterSave.setId(1L);
        userAfterSave.setEmail("bob@gmail.com");
        userAfterSave.setPassword("password");
        userAfterSave.setFirstName("Bob");
        userAfterSave.setLastName("Alister");
        userAfterSave.setAge(21);
        userAfterSave.setDriverLicence("DFG23K34H");
        userAfterSave.setRoles(Set.of(new Role(1L, RoleName.valueOf("USER"))));
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
        Mockito.when(balanceService.createNewBalance(any(User.class))).thenReturn(null);

        RegistrationResponseDto actual = registrationService.register(requestUserDto);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.getMessage().contains("Thanks for the registration"));
    }

    @Test
    void registerWithExistingDriverLicence() {
        RegistrationUserRequestDto requestUserDto = new RegistrationUserRequestDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setDriverLicence("DFG23K34H");
        User userWithSameDriverLicence = new User();
        userWithSameDriverLicence.setDriverLicence("DFG23K34H");
        Mockito.when(userService.findByEmail(requestUserDto.getEmail())).thenReturn(null);
        Mockito.when(userService.findByDriverLicence(requestUserDto.getDriverLicence()))
                .thenReturn(userWithSameDriverLicence);
        Assertions.assertThrows(DriverLicenceAlreadyExistException.class,
                () -> registrationService.register(requestUserDto));
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
        EmailConfirmationResponseDto actual = registrationService.confirmEmail("confirmationToken");
        Assertions.assertTrue(actual.getMessage().contains("confirmed successfully"));
    }

    @Test
    void confirmWithNotExistToken() {
        Mockito.when(confirmationTokenService
                .findByToken("confirmationToken")).thenReturn(null);
        assertThrows(ConfirmationTokenInvalidException.class,
                () -> registrationService.confirmEmail("confirmationToken"));
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
                () -> registrationService.confirmEmail("confirmationToken"));
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
        EmailConfirmationResponseDto actual = registrationService.confirmEmail("confirmationToken");
        Assertions.assertTrue(actual.getMessage().contains("token was expired"));
    }

    @Test
    void resendEmailWithExistUserAndInvalidateEmail() {
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
        Mockito.when(confirmationTokenService.findAllByUser(user))
                .thenReturn(List.of(new ConfirmationToken()));
        Mockito.doNothing().when(confirmationTokenService).delete(any(ConfirmationToken.class));
        RegistrationResponseDto actual = registrationService.resendEmail("bob@gmail.com");
        Assertions.assertNotNull(actual.getUrl());
    }

    @Test
    void resendEmailWithExistAndInvalidateEmailAndNotExistingOldTokenInDb() {
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
        Mockito.when(confirmationTokenService.findAllByUser(user))
                .thenReturn(null);
        RegistrationResponseDto actual = registrationService.resendEmail("bob@gmail.com");
        Assertions.assertNotNull(actual.getUrl());
    }

    @Test
    void resendEmailWithNotExistUser() {
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> registrationService.resendEmail("bob@gmail.com"));
    }

    @Test
    void resendEmailWithValidatedUser() {
        User user = new User();
        user.setStatus(new Status(1L, StatusType.ACTIVE));

        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);
        assertThrows(ConfirmationTokenInvalidException.class,
                () -> registrationService.resendEmail("bob@gmail.com"));
    }
}
