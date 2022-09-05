package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
public class BalanceRequestDto {
    @ApiModelProperty(example = "100.50")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    @NotNull
    @Positive
    private BigDecimal value;
}
