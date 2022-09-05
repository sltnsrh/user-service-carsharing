package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BalanceRequestDto {
    @ApiModelProperty(example = "100.50")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    @NotNull
    private BigDecimal value;
}
