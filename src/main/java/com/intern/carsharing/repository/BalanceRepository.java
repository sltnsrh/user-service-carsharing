package com.intern.carsharing.repository;

import com.intern.carsharing.model.Balance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    @Query(value = "from Balance where user.id = :id")
    Optional<Balance> findByUserId(Long id);
}
