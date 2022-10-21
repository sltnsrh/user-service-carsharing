package com.intern.carsharing.service;

import com.intern.carsharing.model.BlackList;
import java.util.List;

public interface BlackListService {
    BlackList add(BlackList blackList);

    List<BlackList> findAllByUserId(long userId);

    void delete(BlackList blackList);
}
