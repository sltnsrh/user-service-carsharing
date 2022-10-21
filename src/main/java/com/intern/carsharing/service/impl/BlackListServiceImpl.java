package com.intern.carsharing.service.impl;

import com.intern.carsharing.model.BlackList;
import com.intern.carsharing.model.User;
import com.intern.carsharing.repository.BlackListRepository;
import com.intern.carsharing.service.BlackListService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlackListServiceImpl implements BlackListService {
    private final BlackListRepository blackListRepository;

    @Override
    public BlackList add(BlackList blackList) {
        return blackListRepository.save(blackList);
    }

    @Override
    public void delete(BlackList blackList) {
        blackListRepository.delete(blackList);
    }

    @Override
    public List<BlackList> getAllByToken(String token) {
        return blackListRepository.findAllBlackListByJwtToken(token).orElse(List.of());
    }

    @Override
    public void deleteAllExpiredByUser(User user) {
        blackListRepository.findAllByUser(user).orElse(List.of()).stream()
                .filter(blacklist -> blacklist.getExpirationDate().isBefore(LocalDateTime.now()))
                .forEach(blackListRepository::delete);
    }
}
