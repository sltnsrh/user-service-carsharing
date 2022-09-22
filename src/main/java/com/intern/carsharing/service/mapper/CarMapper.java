package com.intern.carsharing.service.mapper;

import com.intern.carsharing.model.dto.response.CarDto;
import com.intern.carsharing.model.dto.response.CarStatisticsResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarStatisticsResponseDto toStatisticsDto(CarDto carDto);
}
