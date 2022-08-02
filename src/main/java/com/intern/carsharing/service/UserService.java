package com.intern.carsharing.service;

import com.intern.carsharing.model.User;

public interface UserService {
    User findByEmail(String email);

    User save(User user);

    User get(Long id);
}
