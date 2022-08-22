package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapperUtil.class})
@RequiredArgsConstructor
public abstract class UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "roles", target = "roles", qualifiedByName = "setUserRole")
    @Mapping(source = "status", target = "status", qualifiedByName = "setStatusActive")
    public abstract User toModel(RegistrationRequestUserDto dto);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToSetString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    public abstract ResponseUserDto toDto(User user);
}
