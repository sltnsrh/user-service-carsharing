package com.intern.carsharing.model.dto.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeStatusRequestDto {
    @ApiModelProperty(example = "active")
    @NotBlank
    private String status;
}
