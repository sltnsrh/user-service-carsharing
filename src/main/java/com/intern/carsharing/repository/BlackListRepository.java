package com.intern.carsharing.repository;

import com.intern.carsharing.model.BlackList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    Optional<List<BlackList>> findAllByUserId(long userId);

    Optional<List<BlackList>> findAllBlackListByJwtToken(String token);
}
