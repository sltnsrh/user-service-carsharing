package com.intern.carsharing.repository;

public interface BlacklistRepository {
    void add(String token, String username);

    boolean isLoggedOut(String token);
}
