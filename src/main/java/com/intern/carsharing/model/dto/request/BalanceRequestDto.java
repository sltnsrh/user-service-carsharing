package com.intern.carsharing.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BalanceRequestDto {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    private BigDecimal value;
}
