package com.intern.carsharing.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {
    @ApiModelProperty(example = "bob@gmail.com")
    private String email;
    @ApiModelProperty(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2IxQGdtYWlsLmNvbSIsInJvbGV"
            + "zIjpbIlVTRVIiXSwiaWF0IjoxNjYwNzY0MTgyLCJleHAiOjE2NjA3NjQ1NDJ9.r8kASRvksi3Ev"
            + "-BS9rQ4L56l_iI7kwIDIy27OIKYp-g")
    private String token;
    @ApiModelProperty(example = "a9dae4ce-3611-42a8-92ed-5d1eaf72fe09")
    private String refreshToken;
    @ApiModelProperty(example = "Your email wasn't confirmed yet.")
    private String message;
    @ApiModelProperty(example = "localhost:8080/resend?email=bob7@gmail.com")
    private String resendUrl;
}
