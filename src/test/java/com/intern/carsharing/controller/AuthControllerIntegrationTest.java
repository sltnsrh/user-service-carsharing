package com.intern.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

class AuthControllerIntegrationTest extends IntegrationTest {
    private RegistrationUserRequestDto registraionRequestDto;

    @BeforeEach()
    void init() {
        registraionRequestDto = new RegistrationUserRequestDto();
        registraionRequestDto.setEmail(USER_EMAIL);
        registraionRequestDto.setPassword("password");
        registraionRequestDto.setRepeatPassword("password");
        registraionRequestDto.setFirstName("Bob");
        registraionRequestDto.setLastName("Alister");
        registraionRequestDto.setAge(21);
        registraionRequestDto.setDriverLicence("DFG23K34H");
        registraionRequestDto.setRole("USER");
    }

    @Test
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerWithValidData() throws Exception {
        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registraionRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerWithNotMatchPassword() throws Exception {
        registraionRequestDto.setPassword("passwor");
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registraionRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerExistUser() throws Exception {
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registraionRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerWithAgeLessThan21() throws Exception {
        registraionRequestDto.setAge(20);
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registraionRequestDto)))
                .andExpect(status().isBadRequest());
    }

}
