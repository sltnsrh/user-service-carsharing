package com.intern.carsharing.repository;

import com.intern.carsharing.model.BlackList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<BalanceRepository, Long> {
    BlackList add(BlackList blackList);

    @Query("from BlackList where user.id = :userId")
    Optional<List<BlackList>> findAllByUserId(long userId);
}
