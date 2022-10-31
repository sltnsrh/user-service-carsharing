package com.intern.carsharing.repository;

import com.intern.carsharing.model.BlackList;

public interface BlacklistRepository {
    void add(BlackList blackList);

    boolean isLoggedOut(String token);
}
