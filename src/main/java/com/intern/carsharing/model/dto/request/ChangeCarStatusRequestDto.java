package com.intern.carsharing.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeCarStatusRequestDto {
    private long carId;
    private String status;
}
