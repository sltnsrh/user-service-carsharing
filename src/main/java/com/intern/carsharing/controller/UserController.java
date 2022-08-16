package com.intern.carsharing.controller;

import com.intern.carsharing.model.dto.request.RequestUserUpdateDto;
import com.intern.carsharing.service.UserService;
import com.intern.carsharing.service.mapper.UserMapper;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody RequestUserUpdateDto requestDto
            ) {
        return new ResponseEntity<>(
                userMapper.toDto((userService.update(id, requestDto))),
                HttpStatus.OK
        );
    }
}
