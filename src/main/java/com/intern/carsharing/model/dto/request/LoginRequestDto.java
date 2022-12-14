package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @ApiModelProperty(example = "bob@gmail.com")
    @NotBlank(message = "Email field can't be empty or blank")
    private String email;
    @ApiModelProperty(example = "password")
    @NotBlank(message = "Password field can't be empty or blank")
    private String password;
}
