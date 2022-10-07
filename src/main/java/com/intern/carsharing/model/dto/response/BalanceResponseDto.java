package com.intern.carsharing.model.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BalanceResponseDto {
    private long userId;
    private String message;
    private BigDecimal value;
    private String currency;
}
