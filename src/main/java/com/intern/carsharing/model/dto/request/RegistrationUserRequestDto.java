package com.intern.carsharing.model.dto.request;

import com.intern.carsharing.lib.FieldsValueMatch;
import com.intern.carsharing.lib.ValidEmail;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@FieldsValueMatch(
        field = "password",
        fieldMatch = "repeatPassword",
        message = "Passwords do not match"
)

@Data
public class RegistrationUserRequestDto {
    @ApiModelProperty(example = "bob@gmail.com", required = true)
    @ValidEmail
    private String email;
    @ApiModelProperty(example = "password", required = true)
    @NotBlank(message = "Password field can't be empty or blank")
    @Size(min = 6, max = 50, message = "Password must be min 6 characters long, max 50")
    private String password;
    @ApiModelProperty(example = "password", required = true)
    private String repeatPassword;
    @ApiModelProperty(example = "Bob", required = true)
    @NotEmpty(message = "First name field can't be empty")
    @Size(min = 3, max = 50,
            message = "The length of the first name must be at least 3 characters, max 50")
    private String firstName;
    @ApiModelProperty(example = "Alister", required = true)
    @NotEmpty(message = "Last name field can't be empty")
    @Size(min = 3, max = 50,
            message = "The length of the last name must be at least 3 characters, max 50")
    private String lastName;
    @ApiModelProperty(notes = "Minimum 21 years", example = "21", required = true)
    @NotNull(message = "Age field can't be null")
    @Min(value = 21, message = "Your age must be at least 21 years old")
    private int age;
    @ApiModelProperty(notes = "Consists of 9 characters", example = "HKJ423KJU", required = true)
    @NotEmpty(message = "Driver licence field can't be empty")
    @Size(min = 9, max = 9, message = "The driver's license number must contain 9 characters")
    private String driverLicence;
    private String status;
    private String role;
}
