package com.intern.carsharing.model.dto.request;

import lombok.Data;

@Data
public class CarRegistrationRequestDto {
    private String brand;
    private String model;
    private int year;
    private double millageKm;
    private double fuelLevelLiter;
    private double fuelConsumptionLitersPer100Km;
    private String licensePlate;
    private String bodyType;
    private String carClass;
    private String status;
    private String engineType;
    private String transmission;
}
