package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import java.util.Set;
import java.util.stream.Collectors;
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
        ResponseUserDto responseUserDto = mapper.map(model, ResponseUserDto.class);
        Set<String> roles = model.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toSet());
        responseUserDto.setRoles(roles);
        responseUserDto.setStatus(model.getStatus().getStatusType().name());
        return responseUserDto;
    }
}
