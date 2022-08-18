package com.intern.carsharing.model.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    @ApiModelProperty(example = "bob@gmail.com")
    private String email;
    @ApiModelProperty(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2IxQGdtYWlsLmNvbSIsInJvbGV"
            + "zIjpbIlVTRVIiXSwiaWF0IjoxNjYwNzY0MTgyLCJleHAiOjE2NjA3NjQ1NDJ9.r8kASRvksi3Ev"
            + "-BS9rQ4L56l_iI7kwIDIy27OIKYp-g")
    private String token;
}
