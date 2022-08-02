package com.intern.carsharing.model.dto.response;

import java.util.Set;
import lombok.Data;

@Data
public class ResponseUserDto {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private int age;
    private String driverLicence;
    private Set<String> roles;
    private String status;
}
