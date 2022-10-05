package com.intern.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import org.junit.jupiter.api.Test;

class AuthControllerIntegrationTest extends IntegrationTest {

    @Test
    void registerWithValidData() throws Exception {
        RegistrationUserRequestDto requestUserDto = new RegistrationUserRequestDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRole("USER");

        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isCreated());
    }

}
