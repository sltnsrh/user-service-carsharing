package com.intern.carsharing.model.dto.request;

import com.intern.carsharing.lib.FieldsValueMatch;
import com.intern.carsharing.lib.ValidEmail;
import java.util.Set;
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
public class RegistrationRequestUserDto {
    @ValidEmail
    private String email;
    @NotBlank(message = "Password field can't be empty or blank")
    @Size(min = 6, message = "Password must be min 6 characters long")
    private String password;
    private String repeatPassword;
    @NotEmpty(message = "First name field can't be empty")
    @Size(min = 3, message = "The length of the first name must be at least 3 characters")
    private String firstName;
    @NotEmpty(message = "Last name field can't be empty")
    @Size(min = 3, message = "The length of the last name must be at least 3 characters")
    private String lastName;
    @NotNull(message = "Age field can't be null")
    @Min(value = 21, message = "Your age must be at least 21 years old")
    private int age;
    @NotEmpty(message = "Driver licence field can't be empty")
    @Size(min = 9, max = 9, message = "The driver's license number must contain 9 characters")
    private String driverLicence;
    @NotNull(message = "Roles field can't be empty")
    private Set<String> roles;
}
