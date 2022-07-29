package com.intern.carsharing.repository;

import com.intern.carsharing.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByStatusType(Status.StatusType statusType);
}
