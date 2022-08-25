package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.model.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapperUtil.class})
@RequiredArgsConstructor
public abstract class UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "roles", target = "roles", qualifiedByName = "setUserRole")
    @Mapping(source = "status", target = "status", qualifiedByName = "setStatusActive")
    public abstract User toModel(RegistrationUserRequestDto dto);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToSetString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    public abstract UserResponseDto toDto(User user);
}
