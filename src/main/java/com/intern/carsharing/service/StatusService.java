package com.intern.carsharing.service;

import com.intern.carsharing.model.Status;

public interface StatusService {
    Status findByStatusType(Status.StatusType statusType);
}
