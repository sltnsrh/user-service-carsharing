package com.intern.carsharing.controller;

import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserInfo(
            @PathVariable("id") Long id
    ) {
        return new ResponseEntity<>(userMapper.toDto(userService.get(id)), HttpStatus.OK);
    }
}
