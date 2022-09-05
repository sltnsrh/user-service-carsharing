package com.intern.carsharing.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StatisticsResponseDto {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal price;
    private Long carId;
    private String carType;
    private Long userId;
}