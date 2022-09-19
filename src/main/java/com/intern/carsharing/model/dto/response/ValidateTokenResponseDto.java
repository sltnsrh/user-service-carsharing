package com.intern.carsharing.model.dto.response;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;

@Data
public class ValidateTokenResponseDto {
    @ApiModelProperty(example = "1")
    private Long userId;
    @ApiModelProperty(example = "{[ADMIN, USER]}")
    private List<String> roles;
}
