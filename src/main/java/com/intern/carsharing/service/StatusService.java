package com.intern.carsharing.service;

import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.util.StatusType;

public interface StatusService {
    Status findByStatusType(StatusType statusType);
}
