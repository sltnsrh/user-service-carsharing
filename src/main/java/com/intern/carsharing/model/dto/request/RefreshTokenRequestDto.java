package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDto {
    @ApiModelProperty(example = "2903037d-e267-4959-962d-00ca195540df")
    private String token;
}
