package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateTokenRequestDto {
    @NotNull
    @NotBlank
    @ApiModelProperty(example = "Bearer eyJhbGciOiJIUzI1NiJ9."
            + "eyJzdWIiOiJib2I3QGdtYWlsL2NjIyOTUwOTJ9."
            + "Uxp0CUIge18rCfgGvNV4P8yENflGKLB9BcbHI8NbAdA")
    private String token;
}
