package com.intern.carsharing.model.dto.response;

import com.intern.carsharing.model.dto.request.CoordinatesDto;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String brand;
    private int constructionYear;
    private double mileageKm;
    private double fuelLevelLiter;
    private double fuelConsumptionLitersPer100Km;
    private String licensePlate;
    private String carBodyStyle;
    private String carClass;
    private String carStatus;
    private String engineType;
    private String machineDriveType;
    private String transmission;
    private Long carOwnerId;
    private CoordinatesDto coordinates;
}
