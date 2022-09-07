package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CarRegistrationRequestDto {
    @ApiModelProperty(example = "BMW")
    private String brand;
    @ApiModelProperty(example = "540i")
    private String model;
    @ApiModelProperty(example = "2019")
    private int year;
    @ApiModelProperty(example = "80500")
    private double millageKm;
    @ApiModelProperty(example = "30.50")
    private double fuelLevelLiter;
    @ApiModelProperty(example = "15.5")
    private double fuelConsumptionLitersPer100Km;
    @ApiModelProperty(example = "BC1234AB")
    private String licensePlate;
    @ApiModelProperty(example = "sedan")
    private String bodyType;
    @ApiModelProperty(example = "E")
    private String carClass;
    @ApiModelProperty(example = "ready")
    private String status;
    @ApiModelProperty(example = "gasoline")
    private String engineType;
    @ApiModelProperty(example = "automatic")
    private String transmission;
}
