package com.intern.carsharing.repository;

import com.intern.carsharing.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    Status findByStatusType(Status.StatusType statusType);
}
