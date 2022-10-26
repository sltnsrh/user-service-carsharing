package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.BlackList;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.BlackListRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlackListServiceImplTest {
    @InjectMocks
    private BlackListServiceImpl blackListService;
    @Mock
    private BlackListRepository blackListRepository;

    @Test
    void addWithValidData() {
        BlackList blackList = new BlackList();
        Mockito.when(blackListRepository.save(blackList)).thenReturn(blackList);
        BlackList actual = blackListService.add(blackList);
        Assertions.assertEquals(blackList, actual);
    }

    @Test
    void deleteWithValidData() {
        BlackList blackList = new BlackList();
        blackListService.delete(blackList);
        Mockito.verify(blackListRepository, Mockito.times(1)).delete(blackList);
    }

    @Test
    void deleteAllExpiredByUserWithExistingBlackList() {
        User user = new User();
        BlackList blackList = new BlackList();
        blackList.setUser(user);
        blackList.setExpirationDate(LocalDateTime.MIN);
        Mockito.when(blackListRepository.findAllByUser(user))
                .thenReturn(Optional.of(List.of(blackList)));
        blackListService.deleteAllExpiredByUser(user);
        Mockito.verify(blackListRepository, Mockito.times(1)).delete(blackList);
    }
}
