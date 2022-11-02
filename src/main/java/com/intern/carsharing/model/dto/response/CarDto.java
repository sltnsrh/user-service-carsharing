package com.intern.carsharing.model.dto.response;

import com.intern.carsharing.model.dto.request.CoordinatesDto;
import lombok.Data;

@Data
public class CarDto {
    private long id;
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
    private long ownerId;
    private CoordinatesDto coordinates;
}
