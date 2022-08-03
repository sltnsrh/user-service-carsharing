package com.intern.carsharing.model.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Email field can't be empty or blank")
    private String email;
    @NotBlank(message = "Password field can't be empty or blank")
    private String password;
}
