package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChangeCarStatusRequestDto {
    @JsonProperty("carStatus")
    private String status;
}
