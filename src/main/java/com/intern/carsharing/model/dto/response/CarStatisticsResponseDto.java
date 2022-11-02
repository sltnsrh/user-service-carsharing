package com.intern.carsharing.model.dto.response;

import com.intern.carsharing.model.dto.request.CoordinatesDto;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CarStatisticsResponseDto {
    private Long id;
    private String brand;
    private int constructionYear;
    private double mileageKm;
    private double fuelLevelLiter;
    private double fuelConsumptionLitersPer100Km;
    private String licensePlate;
    private String bodyStyle;
    private String classOfCar;
    private String status;
    private String engineType;
    private String machineDriveType;
    private String transmission;
    private Long ownerId;
    private CoordinatesDto coordinates;
    private int tripsNumber;
    private BigDecimal generalIncome;
    private List<OrderDto> orders;
}
