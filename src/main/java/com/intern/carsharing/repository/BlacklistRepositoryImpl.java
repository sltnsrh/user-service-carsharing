package com.intern.carsharing.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlacklistRepositoryImpl implements BlacklistRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.token.expired.ms}")
    private long tokenExpPeriodMs;

    @Override
    public void add(String token, String username) {
        redisTemplate.opsForValue().set(token, username, tokenExpPeriodMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isLoggedOut(String token) {
        return redisTemplate.opsForValue().get(token) != null;
    }
}
