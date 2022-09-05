package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BalanceRequestDto {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    @NotNull
    private BigDecimal value;
}
