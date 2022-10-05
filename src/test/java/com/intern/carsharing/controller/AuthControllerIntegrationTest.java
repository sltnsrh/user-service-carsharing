package com.intern.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationUserRequestDto;
import com.intern.carsharing.repository.ConfirmationTokenRepository;
import com.intern.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

class AuthControllerIntegrationTest extends IntegrationTest {
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    private UserRepository userRepository;
    private RegistrationUserRequestDto registrationRequestDto;

    @BeforeEach()
    void init() {
        registrationRequestDto = new RegistrationUserRequestDto();
        registrationRequestDto.setEmail(USER_EMAIL);
        registrationRequestDto.setPassword("password");
        registrationRequestDto.setRepeatPassword("password");
        registrationRequestDto.setFirstName("Bob");
        registrationRequestDto.setLastName("Alister");
        registrationRequestDto.setAge(21);
        registrationRequestDto.setDriverLicence("DFG23K34H");
        registrationRequestDto.setRole("USER");
    }

    @Test
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerWithValidData() throws Exception {
        mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerWithNotMatchPassword() throws Exception {
        registrationRequestDto.setPassword("passwor");
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerExistUser() throws Exception {
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerWithAgeLessThan21() throws Exception {
        registrationRequestDto.setAge(20);
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void loginWithValidData() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(USER_EMAIL, "password");
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void loginWithInvalidPassword() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(USER_EMAIL, "passwor");
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithEmptyEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("", "password");
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithNotExistEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("notexist@gmail.com", "password");
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void confirmEmailWithValidToken() throws Exception {
        mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isCreated());
        User user = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String confirmationToken = confirmationTokenRepository
                .findAllByUser(user).orElseThrow()
                .stream()
                .findFirst().orElseThrow()
                .getToken();
        mockMvc.perform(get("/confirm?token=" + confirmationToken))
                .andExpect(status().isOk());
    }
}
