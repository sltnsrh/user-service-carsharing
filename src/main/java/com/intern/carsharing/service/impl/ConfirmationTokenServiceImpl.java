package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.repository.ConfirmationTokenRepository;
import com.intern.carsharing.service.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Override
    public ConfirmationToken findByToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElse(null);
    }
}
