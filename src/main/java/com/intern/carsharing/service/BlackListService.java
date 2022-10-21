package com.intern.carsharing.service;

import com.intern.carsharing.model.BlackList;
import com.intern.carsharing.model.User;
import java.util.List;

public interface BlackListService {
    BlackList add(BlackList blackList);

    void delete(BlackList blackList);

    List<BlackList> getAllByToken(String token);

    void deleteAllExpiredByUser(User user);
}
