package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper implements RequestDtoMapper<User, RegistrationRequestUserDto>,
        ResponseDtoMapper<User, ResponseUserDto> {
    private final ModelMapper mapper;

    @Override
    public User toModel(RegistrationRequestUserDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public ResponseUserDto toDto(User model) {
        return mapper.map(model, ResponseUserDto.class);
    }
}
