package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.Status;
import com.intern.carsharing.repository.StatusRepository;
import com.intern.carsharing.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {
    private final StatusRepository statusRepository;

    @Override
    public Status findByStatusType(Status.StatusType statusType) {
        return statusRepository.findByStatusType(statusType);
    }
}
