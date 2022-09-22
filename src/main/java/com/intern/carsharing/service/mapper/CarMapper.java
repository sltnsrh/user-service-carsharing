package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.dto.response.CarDto;
import com.intern.carsharing.model.dto.response.CarStatisticsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CarMapper {
    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);

    CarStatisticsResponseDto toStatisticsDto(CarDto carDto);
}
