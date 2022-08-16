package com.intern.carsharing.service;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RequestUserUpdateDto;

public interface UserService {
    User findByEmail(String email);

    User save(User user);

    User get(Long id);

    User update(Long id, RequestUserUpdateDto updateDto);
}
