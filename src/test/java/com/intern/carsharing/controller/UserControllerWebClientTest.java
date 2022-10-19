package com.intern.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.response.CarDto;
import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;

public class UserControllerWebClientTest extends IntegrationTest {
    private static final String CAR_OWNER_EMAIL = "owner@gmail.com";
    private final MockWebServer mockCarWebServer = new MockWebServer();
    private final MockWebServer mockBackofficeWebServer = new MockWebServer();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void start() throws IOException {
        mockCarWebServer.start(8084);
    }

    @AfterEach
    void close() throws IOException {
        mockCarWebServer.shutdown();
    }

    @Test
    @Sql(value = "/add_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStatisticsWithValidData() throws Exception {
        mockBackofficeWebServer.start(8082);
        mockBackofficeWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{}"));
        User userFromDb = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider.createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(get("/users/{id}/statistics", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk());
        mockBackofficeWebServer.shutdown();
    }

    @Test
    @Sql(value = "/add_car-owner.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_car-owner.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCarStatisticsWithValidData() throws Exception {
        User carOwnerFromDb = userRepository.findUserByEmail(CAR_OWNER_EMAIL).orElseThrow();
        CarDto carDto = new CarDto();
        carDto.setCarOwnerId(carOwnerFromDb.getId());
        mockCarWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(objectMapper.writeValueAsString(carDto)));
        OrderDto orderDto = new OrderDto();
        orderDto.setPrice(BigDecimal.valueOf(100));
        mockBackofficeWebServer.start(8082);
        mockBackofficeWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(objectMapper.writeValueAsString(List.of(orderDto))));
        String jwt = jwtTokenProvider
                .createToken(carOwnerFromDb.getEmail(), carOwnerFromDb.getRoles());
        mockMvc.perform(get("/users/{userId}/cars/{carId}", carOwnerFromDb.getId(), 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk());
        mockBackofficeWebServer.shutdown();
    }

    @Test
    @Sql(value = "/add_car-owner.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_car-owner.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCarStatisticsWithValidDataAndNoOrders() throws Exception {
        User carOwnerFromDb = userRepository.findUserByEmail(CAR_OWNER_EMAIL).orElseThrow();
        CarDto carDto = new CarDto();
        carDto.setCarOwnerId(carOwnerFromDb.getId());
        mockCarWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(objectMapper.writeValueAsString(carDto)));
        mockBackofficeWebServer.start(8082);
        mockBackofficeWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json"));
        String jwt = jwtTokenProvider
                .createToken(carOwnerFromDb.getEmail(), carOwnerFromDb.getRoles());
        mockMvc.perform(get("/users/{userId}/cars/{carId}", carOwnerFromDb.getId(), 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk());
        mockBackofficeWebServer.shutdown();
    }

    @Test
    @Sql(value = "/add_car-owner.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_car-owner.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void addCarToRentWithValidData() throws Exception {
        mockCarWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{}"));
        CarRegistrationRequestDto requestDto = new CarRegistrationRequestDto();
        User carOwnerFromDb = userRepository
                .findUserByEmail(CAR_OWNER_EMAIL).orElseThrow();
        String jwt = jwtTokenProvider
                .createToken(carOwnerFromDb.getEmail(), carOwnerFromDb.getRoles());
        mockMvc.perform(post("/users/{userId}/cars", carOwnerFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @Sql(value = "/add_car-owner.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/delete_car-owner.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void changeCarStatusWithValidData() throws Exception {
        User carOwnerFromDb = userRepository.findUserByEmail(CAR_OWNER_EMAIL).orElseThrow();
        CarDto carDto = new CarDto();
        carDto.setCarStatus("LOCKED");
        carDto.setCarOwnerId(carOwnerFromDb.getId());
        mockCarWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(objectMapper.writeValueAsString(carDto)));
        String jwt = jwtTokenProvider
                .createToken(carOwnerFromDb.getEmail(), carOwnerFromDb.getRoles());
        mockCarWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{}"));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        mockMvc.perform(patch("/users/{userId}/cars/{carId}", carOwnerFromDb.getId(), 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
