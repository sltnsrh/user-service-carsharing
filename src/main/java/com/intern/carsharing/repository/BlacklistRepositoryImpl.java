package com.intern.carsharing.repository;

import com.intern.carsharing.model.BlackList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlacklistRepositoryImpl implements BlacklistRepository {
    private static final String KEY = "Blacklist";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void add(BlackList blackList) {
        redisTemplate.opsForHash().put(KEY, blackList.getJwtToken(), blackList.getUserId());
    }

    @Override
    public boolean isLoggedOut(String token) {
        return redisTemplate.opsForHash().hasKey(KEY, token);
    }
}
