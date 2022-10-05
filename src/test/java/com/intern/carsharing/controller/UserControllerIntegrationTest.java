package com.intern.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.ChangeStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.UserResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.BalanceRepository;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

public class UserControllerIntegrationTest extends IntegrationTest {
    private static final String ADMIN_EMAIL = "admin@carsharing.com";
    private static final String NEW_USER_EMAIL = "newuser@gmail.com";
    private static final Set<Role> adminRoleSet = Set.of(new Role(1L, RoleName.ADMIN));
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private BalanceRepository balanceRepository;

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserInfoWithValidUserData() throws Exception {
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        MvcResult mvcResult = mockMvc.perform(get("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        UserResponseDto actualDto = objectMapper
                .readValue(actualResponseBody, UserResponseDto.class);
        Assertions.assertEquals(userFromDb.getId(), actualDto.getId());
        Assertions.assertEquals(userFromDb.getEmail(), actualDto.getEmail());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserInfoWithAnotherId() throws Exception {
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(get("/users/{id}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void getUserInfoWithNotExistUserId() throws Exception {
        String jwt = jwtTokenProvider.createToken(ADMIN_EMAIL, adminRoleSet);
        mockMvc.perform(get("/users/{id}", 1000)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserInfoWithInvalidToken() throws Exception {
        mockMvc.perform(get("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void userUpdateEmailWithValidDataAndToken() throws Exception {
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(NEW_USER_EMAIL);
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        MvcResult mvcResult = mockMvc.perform(put("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andReturn();
        UserResponseDto actualResponseDto = objectMapper
                .readValue(
                        mvcResult.getResponse().getContentAsString(),
                        UserResponseDto.class
                );
        Assertions.assertEquals(userUpdateDto.getEmail(), actualResponseDto.getEmail());
    }

    @Test
    void userUpdateWithInvalidId() throws Exception {
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(NEW_USER_EMAIL);
        userUpdateDto.setFirstName("Bob");
        userUpdateDto.setLastName("Alister");
        userUpdateDto.setAge(21);
        userUpdateDto.setDriverLicence("JHG847JHG");
        String jwt = jwtTokenProvider.createToken(ADMIN_EMAIL, adminRoleSet);
        mockMvc.perform(put("/users/update/{id}", 2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void userUpdateWithExistingEmail() throws Exception {
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(ADMIN_EMAIL);
        userUpdateDto.setFirstName("Bob");
        userUpdateDto.setLastName("Alister");
        userUpdateDto.setAge(21);
        userUpdateDto.setDriverLicence("JHG847JHG");
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(put("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void userUpdateWithExistingEmailAndSameId() throws Exception {
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(userFromDb.getEmail());
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(put("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void userUpdateAgeWithValidData() throws Exception {
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(userFromDb.getEmail());
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(50);
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        MvcResult mvcResult = mockMvc.perform(put("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andReturn();
        UserResponseDto actualResponseDto = objectMapper
                .readValue(
                        mvcResult.getResponse().getContentAsString(),
                        UserResponseDto.class
                );
        Assertions.assertEquals(userUpdateDto.getAge(), actualResponseDto.getAge());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void changeStatusWithValidData() throws Exception {
        ChangeStatusRequestDto requestDto = new ChangeStatusRequestDto();
        requestDto.setStatus("blocked");
        String jwt = jwtTokenProvider.createToken(ADMIN_EMAIL, adminRoleSet);
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        mockMvc.perform(patch("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        Assertions.assertEquals(StatusType.BLOCKED, userFromDb.getStatus().getStatusType());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void toBalanceWithValidData() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(patch("/users/{id}/to-balance", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        Balance actualBalance = balanceRepository.findByUserId(userFromDb.getId()).orElseThrow();
        Assertions.assertEquals(0, requestDto.getValue().compareTo(actualBalance.getValue()));
    }

    @Test
    @Sql(scripts = {"/add_user.sql", "/charge_user_balance200.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void fromBalanceEnoughMoneyCase() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        String jwt = jwtTokenProvider
                .createToken(ADMIN_EMAIL, adminRoleSet);
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        mockMvc.perform(patch(
                "/users/{id}/from-balance", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        Balance actualBalance = balanceRepository.findByUserId(userFromDb.getId()).orElseThrow();
        Assertions.assertEquals(100.00, actualBalance.getValue().doubleValue());
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void fromBalanceNotEnoughMoneyCase() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        String jwt = jwtTokenProvider
                .createToken(ADMIN_EMAIL, adminRoleSet);
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        mockMvc.perform(patch(
                "/users/{id}/from-balance", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
