package com.intern.carsharing.repository;

import com.intern.carsharing.model.ConfirmationToken;
import com.intern.carsharing.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    Optional<List<ConfirmationToken>> findAllByUser(User user);
}
