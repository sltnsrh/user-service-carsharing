package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.exception.CarIsRentedException;
import com.intern.carsharing.exception.CarNotFoundException;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.response.OrderDto;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.mapper.CarMapper;
import com.intern.carsharing.service.mapper.CarMapperImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class CarClientServiceImplTest {
    private static final String CAR_BODY_SIMPLE = "{\"id\": 1,\"carOwnerId\": 1}";
    private static final String CAR_LOCKED_BODY_SIMPLE =
            "{\"id\": 1,\"carStatus\": \"LOCKED\",\"carOwnerId\": 1}";
    @InjectMocks
    private CarClientServiceImpl carClientService;
    @Mock
    private BackofficeClientServiceImpl backofficeClientService;
    @Mock
    private PermissionService permissionService;
    @Spy
    private final CarMapper carMapper = new CarMapperImpl();
    @Spy
    private final WebClient carClient = WebClient.create("http://localhost:8084");
    private final MockWebServer mockWebServer = new MockWebServer();

    @BeforeEach
    void start() throws IOException {
        mockWebServer.start(8084);
    }

    @AfterEach
    void close() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarId() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_BODY_SIMPLE));
        OrderDto order = new OrderDto();
        order.setCarId(1L);
        order.setPrice(BigDecimal.valueOf(100));
        Mockito.when(backofficeClientService.getAllCarOrders(any(), any()))
                .thenReturn(List.of(order));
        ResponseEntity<Object> actual = carClientService.getCarStatistics(
                1L, 1L, null, null, null);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void getCarStatisticsWithInvalidCarId() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));
        Assertions.assertThrows(CarNotFoundException.class,
                () -> carClientService.getCarStatistics(
                        1L, 1L, null, null, null));
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarIdAndNoOrders() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_BODY_SIMPLE));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-type", "application/json"));
        ResponseEntity<Object> actual = carClientService.getCarStatistics(
                1L, 1L, null, null, null);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarNotBelongUser() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_BODY_SIMPLE));
        Assertions.assertThrows(CarNotFoundException.class,
                () -> carClientService.getCarStatistics(
                        2L, 1L, null, null, null));
    }

    @Test
    void addCarToRentWithValidDataAndUserId() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_BODY_SIMPLE));
        CarRegistrationRequestDto requestDto = new CarRegistrationRequestDto();
        ResponseEntity<Object> actual = carClientService.addCarToRent(1L, requestDto);
        Assertions.assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
    }

    @Test
    void addCarToRentWithInvalidData() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));
        CarRegistrationRequestDto requestDto = new CarRegistrationRequestDto();
        ResponseEntity<Object> actual = carClientService.addCarToRent(1L, requestDto);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void changeCarStatusWithValidData() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_LOCKED_BODY_SIMPLE));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-type", "application/json")
                .setBody("{\"carStatus\": \"FREE\"}"));
        ResponseEntity<Object> actual = carClientService.changeCarStatus(1L, 1L, requestDto);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertTrue(Objects.requireNonNull(actual.getBody()).toString().contains("FREE"));
    }

    @Test
    void changeCarStatusWithRentedCar() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"carStatus\": \"RENTED\",\n"
                        + "    \"carOwnerId\": 1\n"
                        + "}"));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        Assertions.assertThrows(CarIsRentedException.class,
                () -> carClientService.changeCarStatus(
                        1L, 1L, requestDto));
    }

    @Test
    void changeCarStatusWithBadRequest() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody(CAR_LOCKED_BODY_SIMPLE));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400));
        ResponseEntity<Object> actual = carClientService.changeCarStatus(1L, 1L, requestDto);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }
}
