package com.intern.carsharing.model.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
