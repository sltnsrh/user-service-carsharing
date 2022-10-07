package com.intern.carsharing.model.dto.response;

import lombok.Data;

@Data
public class EmailConfirmationResponseDto {
    private String email;
    private String message;
}
