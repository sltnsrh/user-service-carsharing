package com.intern.carsharing.model.dto.request;

import com.intern.carsharing.lib.ValidEmail;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDto {
    @ApiModelProperty(example = "bob@gmail.com")
    @ValidEmail
    private String email;
    @ApiModelProperty(example = "Bob")
    @NotEmpty(message = "First name field can't be empty")
    @Size(min = 3, max = 50,
            message = "The length of the first name must be at least 3 characters, max 50")
    private String firstName;
    @ApiModelProperty(example = "Alister")
    @NotEmpty(message = "Last name field can't be empty")
    @Size(min = 3, max = 50,
            message = "The length of the last name must be at least 3 characters, max 50")
    private String lastName;
    @ApiModelProperty(example = "21")
    @NotNull(message = "Age field can't be null")
    @Min(value = 21, message = "Your age must be at least 21 years old")
    private int age;
    @ApiModelProperty(notes = "Consists of 9 characters", example = "HKJ423234", required = true)
    @NotEmpty(message = "Driver licence field can't be empty")
    @Pattern(regexp = "[A-Z]{3}\\d{6}",
            message = "A driver's license number must match the pattern: ABC123456")
    private String driverLicence;
}
