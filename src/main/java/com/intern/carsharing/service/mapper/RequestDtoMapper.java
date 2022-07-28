package com.intern.carsharing.service.mapper;

public interface RequestDtoMapper<M, D> {
    M toModel(D dto);
}
