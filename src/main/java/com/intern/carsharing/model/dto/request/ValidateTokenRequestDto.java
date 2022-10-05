package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenRequestDto {
    @NotNull
    @NotBlank
    @ApiModelProperty(example = "Bearer eyJhbGciOiJIUzI1NiJ9."
            + "eyJzdWIiOiJib2I3QGdtYWlsL2NjIyOTUwOTJ9."
            + "Uxp0CUIge18rCfgGvNV4P8yENflGKLB9BcbHI8NbAdA")
    private String token;
}
