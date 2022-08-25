package com.intern.carsharing.model.dto.response;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDto {
    @ApiModelProperty(example = "1")
    private Long id;
    @ApiModelProperty(example = "bob@gmail.com")
    private String email;
    @ApiModelProperty(example = "sjfh3hu38hf3h9hohglhgsiuhrhg7d87g6d87")
    private String password;
    @ApiModelProperty(example = "Bob")
    private String firstName;
    @ApiModelProperty(example = "Alister")
    private String lastName;
    @ApiModelProperty(example = "21")
    private int age;
    @ApiModelProperty(example = "HKJ423KJU")
    private String driverLicence;
    @ApiModelProperty(example = "[\"USER\"]")
    private Set<String> roles;
    @ApiModelProperty(example = "ENABLE")
    private String status;
}
