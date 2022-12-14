package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CarRegistrationRequestDto {
    @ApiModelProperty(example = "BMW")
    private String brand;
    @ApiModelProperty(example = "540i")
    private String model;
    @ApiModelProperty(example = "2019")
    @JsonProperty("constructionYear")
    private int year;
    @ApiModelProperty(example = "80500")
    private double mileageKm;
    @ApiModelProperty(example = "30.50")
    private double fuelLevelLiter;
    @ApiModelProperty(example = "15.5")
    private double fuelConsumptionLitersPer100Km;
    @ApiModelProperty(example = "BC1234AB")
    private String licensePlate;
    @ApiModelProperty(example = "sedan")
    @JsonProperty("bodyStyle")
    private String bodyType;
    @ApiModelProperty(example = "REAR_WHEEL_DRIVE")
    @JsonProperty("machineDriveType")
    private String wheelsDriveType;
    @ApiModelProperty(example = "PREMIUM")
    private String classOfCar;
    @ApiModelProperty(example = "FREE")
    private String status;
    @ApiModelProperty(example = "gasoline")
    private String engineType;
    @ApiModelProperty(example = "automatic")
    private String transmission;
    @ApiModelProperty("\"latitude\": 49.84, \"longitude\": 24.07")
    private CoordinatesDto coordinates;
    @ApiModelProperty(example = "1")
    private Long ownerId;
}
