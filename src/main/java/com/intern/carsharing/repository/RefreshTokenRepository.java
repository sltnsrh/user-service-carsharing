package com.intern.carsharing.repository;

import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.User;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<List<RefreshToken>> findAllByUser(User user);

    Optional<RefreshToken> findByUserEmail(String email);

    Optional<List<RefreshToken>> findAllByUserEmail(String email);

    @Modifying
    void delete(@NotNull RefreshToken refreshToken);
}
