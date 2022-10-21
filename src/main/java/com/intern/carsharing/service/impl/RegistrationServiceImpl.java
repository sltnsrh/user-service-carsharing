package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ConfirmationTokenInvalidException;
import com.intern.carsharing.exception.DriverLicenceAlreadyExistException;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.EmailConfirmationResponseDto;
import com.intern.carsharing.model.dto.response.RegistrationResponseDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.service.AuthResponseBuilder;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.ConfirmationTokenService;
import com.intern.carsharing.service.RegistrationService;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final BalanceService balanceService;
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final AuthResponseBuilder responseBuilder;

    @Override
    @Transactional
    public RegistrationResponseDto register(RegistrationUserRequestDto requestUserDto) {
        String email = requestUserDto.getEmail();
        User user = userService.findByEmail(email);
        if (user != null) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
        user = userService.findByDriverLicence(requestUserDto.getDriverLicence());
        if (user != null) {
            throw new DriverLicenceAlreadyExistException(requestUserDto.getDriverLicence()
                    + " licence number is already exists.");
        }
        user = getUserFromDtoWithEncodedPassword(requestUserDto);
        balanceService.createNewBalance(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return responseBuilder.getRegistrationResponseMessage(confirmationToken.getToken());
    }

    private User getUserFromDtoWithEncodedPassword(RegistrationUserRequestDto dto) {
        User user = userMapper.toModel(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        return userService.save(user);
    }

    @Override
    @Transactional
    public EmailConfirmationResponseDto confirmEmail(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.findByToken(token);
        if (confirmationToken == null) {
            throw new ConfirmationTokenInvalidException("Confirmation token doesn't exist.");
        }
        String email = confirmationToken.getUser().getEmail();
        if (confirmationToken.getConfirmedAt() != null) {
            throw new ConfirmationTokenInvalidException(
                    "Email " + email + " was already confirmed."
            );
        }
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            return responseBuilder.getTokenExpiredMessage(email);
        }
        confirmationTokenService.setConfirmDate(confirmationToken);
        userService.changeStatus(confirmationToken.getUser().getId(), StatusType.ACTIVE);
        return responseBuilder.getConfirmedSuccessfullyMessage(email);
    }

    @Override
    @Transactional
    public RegistrationResponseDto resendEmail(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email: " + email + " doesn't exist");
        }
        checkIfUserStatusIsInvalidate(user, email);
        checkAndDeleteOldConfirmationTokens(user);
        ConfirmationToken confirmationToken = confirmationTokenService.create(user);
        return responseBuilder.getRegistrationResponseMessage(confirmationToken.getToken());
    }

    private void checkIfUserStatusIsInvalidate(User user, String email) {
        if (!user.getStatus().getStatusType().equals(StatusType.INVALIDATE)) {
            throw new ConfirmationTokenInvalidException(
                    "Your email " + email + " was already confirmed."
            );
        }
    }

    private void checkAndDeleteOldConfirmationTokens(User user) {
        List<ConfirmationToken> confirmationTokenList =
                confirmationTokenService.findAllByUser(user);
        if (confirmationTokenList != null) {
            confirmationTokenList.forEach(confirmationTokenService::delete);
        }
    }
}
