package com.intern.carsharing.model.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeStatusRequestDto {
    @NotBlank
    private String status;
}
