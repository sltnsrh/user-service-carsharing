package com.intern.carsharing.service.mapper;

public interface ResponseDtoMapper<M, D> {
    D toDto(M model);
}
