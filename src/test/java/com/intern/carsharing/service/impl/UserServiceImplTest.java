package com.intern.carsharing.service.impl;

import static org.mockito.ArgumentMatchers.any;

import com.intern.carsharing.exception.CarIsRentedException;
import com.intern.carsharing.exception.CarNotFoundException;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.CarRegistrationRequestDto;
import com.intern.carsharing.model.dto.request.ChangeCarStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.service.BalanceService;
import com.intern.carsharing.service.PermissionService;
import com.intern.carsharing.service.StatusService;
import com.intern.carsharing.service.mapper.CarMapper;
import com.intern.carsharing.service.mapper.CarMapperImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
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
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private BalanceService balanceService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StatusService statusService;
    @Spy
    private final CarMapper carMapper = new CarMapperImpl();
    @Spy
    private final WebClient backofficeServiceClient = WebClient.create("http://localhost:8082");
    private final MockWebServer mockWebServer = new MockWebServer();

    @Test
    void updateWithValidUser() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setEmail("newmail@gmail.com");
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findUserByEmail(userUpdateRequestDto.getEmail()))
                .thenReturn(Optional.of(user));
        user.setEmail(userUpdateRequestDto.getEmail());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        User actual = userService.update(1L, userUpdateRequestDto);
        Assertions.assertEquals(userUpdateRequestDto.getEmail(), actual.getEmail());
    }

    @Test
    void changeStatusOfExistingUser() {
        Status statusToSet = new Status(1L, StatusType.BLOCKED);
        User user = new User();
        user.setStatus(new Status(2L, StatusType.ACTIVE));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(statusService.findByStatusType(StatusType.BLOCKED)).thenReturn(statusToSet);
        user.setStatus(statusToSet);
        Mockito.when(userService.save(any(User.class))).thenReturn(user);
        User actual = userService.changeStatus(1L, StatusType.BLOCKED);
        Assertions.assertEquals(StatusType.BLOCKED, actual.getStatus().getStatusType());
    }

    @Test
    void toBalancePutMoneyOnExistBalance() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(0));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        Mockito.when(balanceService.save(any(Balance.class))).thenReturn(null);
        Mockito.mock(permissionService.getClass());
        String actual = userService.toBalance(1L, balanceRequestDto);
        Assertions.assertNotNull(actual);
    }

    @Test
    void fromBalanceEnoughMoneyCase() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(100));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        Mockito.when(balanceService.save(any(Balance.class))).thenReturn(null);
        String actual = userService.fromBalance(1L, balanceRequestDto);
        Assertions.assertEquals("100 UAH were debited from the balance of the user with id 1",
                actual);
    }

    @Test
    void fromBalanceNotEnoughMoney() {
        BalanceRequestDto balanceRequestDto = new BalanceRequestDto();
        balanceRequestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(99));
        balance.setCurrency("UAH");
        Mockito.when(balanceService.findByUserId(1L)).thenReturn(balance);
        String actual = userService.fromBalance(1L, balanceRequestDto);
        Assertions.assertEquals("Not enough money on balance for a transaction",
                actual);
    }

    @Test
    void getTripStatisticsWithValidUserIdAndParameters() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\"trip\":\"1\"}"));

        Object actual = userService.getTripStatistics(
                1L,
                "2022-01-01",
                "2022-02-01",
                "PREMIUM"
        );
        mockWebServer.shutdown();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("{trip=1}", actual.toString());
    }

    @Test
    void getTripStatisticsWithValidUserId() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\"trip\":\"1\"}"));

        Object actual = userService.getTripStatistics(1L, null, null, null);
        mockWebServer.shutdown();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("{trip=1}", actual.toString());
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarId() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"brand\": \"Renault\",\n"
                        + "    \"model\": \"Megan 8\",\n"
                        + "    \"constructionYear\": 2020,\n"
                        + "    \"mileageKm\": 18001.0,\n"
                        + "    \"fuelLevelLiter\": 30.3,\n"
                        + "    \"fuelConsumptionLitersPer100Km\": 7.5,\n"
                        + "    \"licensePlate\": \"ВС1221СР\",\n"
                        + "    \"carBodyStyle\": \"SEDAN\",\n"
                        + "    \"carClass\": \"MEDIUM\",\n"
                        + "    \"carStatus\": \"LOCKED\",\n"
                        + "    \"engineType\": \"DIESEL\",\n"
                        + "    \"machineDriveType\": \"FRONT_WHEEL_DRIVE\",\n"
                        + "    \"transmission\": \"MANUAL_TRANSMISSION\",\n"
                        + "    \"carOwnerId\": 1,\n"
                        + "    \"coordinates\": {\n"
                        + "        \"latitude\": 49.84,\n"
                        + "        \"longitude\": 24.07\n"
                        + "    }\n"
                        + "}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-type", "application/json")
                .setBody("[\n"
                        + "        {\n"
                        + "            \"startDateTime\": \"2021-11-10 00:00:00\",\n"
                        + "            \"endDateTime\": \"2021-11-13 00:00:00\",\n"
                        + "            \"price\": 1500,\n"
                        + "            \"carId\": 1\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"startDateTime\": \"2022-01-02 00:00:00\",\n"
                        + "            \"endDateTime\": \"2022-01-03 00:00:00\",\n"
                        + "            \"price\": 700,\n"
                        + "            \"carId\": 1\n"
                        + "        }\n"
                        + "    ]"));

        ResponseEntity<Object> actual = userService.getCarStatistics(
                1L, 1L, null, null, null);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void getCarStatisticsWithInvalidCarId() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));
        Assertions.assertThrows(CarNotFoundException.class,
                () -> userService.getCarStatistics(
                        1L, 1L, null, null, null));
        mockWebServer.shutdown();
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarIdAndNoOrders() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"brand\": \"Renault\",\n"
                        + "    \"model\": \"Megan 8\",\n"
                        + "    \"constructionYear\": 2020,\n"
                        + "    \"mileageKm\": 18001.0,\n"
                        + "    \"fuelLevelLiter\": 30.3,\n"
                        + "    \"fuelConsumptionLitersPer100Km\": 7.5,\n"
                        + "    \"licensePlate\": \"ВС1221СР\",\n"
                        + "    \"carBodyStyle\": \"SEDAN\",\n"
                        + "    \"carClass\": \"MEDIUM\",\n"
                        + "    \"carStatus\": \"LOCKED\",\n"
                        + "    \"engineType\": \"DIESEL\",\n"
                        + "    \"machineDriveType\": \"FRONT_WHEEL_DRIVE\",\n"
                        + "    \"transmission\": \"MANUAL_TRANSMISSION\",\n"
                        + "    \"carOwnerId\": 1,\n"
                        + "    \"coordinates\": {\n"
                        + "        \"latitude\": 49.84,\n"
                        + "        \"longitude\": 24.07\n"
                        + "    }\n"
                        + "}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-type", "application/json"));
        ResponseEntity<Object> actual = userService.getCarStatistics(
                1L, 1L, null, null, null);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void getCarStatisticsWithValidUserIdAndCarNotBelongUser() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"brand\": \"Renault\",\n"
                        + "    \"model\": \"Megan 8\",\n"
                        + "    \"constructionYear\": 2020,\n"
                        + "    \"mileageKm\": 18001.0,\n"
                        + "    \"fuelLevelLiter\": 30.3,\n"
                        + "    \"fuelConsumptionLitersPer100Km\": 7.5,\n"
                        + "    \"licensePlate\": \"ВС1221СР\",\n"
                        + "    \"carBodyStyle\": \"SEDAN\",\n"
                        + "    \"carClass\": \"MEDIUM\",\n"
                        + "    \"carStatus\": \"LOCKED\",\n"
                        + "    \"engineType\": \"DIESEL\",\n"
                        + "    \"machineDriveType\": \"FRONT_WHEEL_DRIVE\",\n"
                        + "    \"transmission\": \"MANUAL_TRANSMISSION\",\n"
                        + "    \"carOwnerId\": 2,\n"
                        + "    \"coordinates\": {\n"
                        + "        \"latitude\": 49.84,\n"
                        + "        \"longitude\": 24.07\n"
                        + "    }\n"
                        + "}"));
        Assertions.assertThrows(CarNotFoundException.class,
                () -> userService.getCarStatistics(
                1L, 1L, null, null, null));
        mockWebServer.shutdown();
    }

    @Test
    void addCarToRentWithValidDataAndUserId() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-type", "application/json")
                .setBody("{\"id\": 1, \n \"brand\":\"BMW\"}"
                ));
        CarRegistrationRequestDto requestDto = new CarRegistrationRequestDto();
        ResponseEntity<Object> actual = userService.addCarToRent(1L, requestDto);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
    }

    @Test
    void addCarToRentWithInvalidData() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));
        CarRegistrationRequestDto requestDto = new CarRegistrationRequestDto();
        ResponseEntity<Object> actual = userService.addCarToRent(1L, requestDto);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void changeCarStatusWithValidData() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"carStatus\": \"LOCKED\",\n"
                        + "    \"carOwnerId\": 1\n"
                        + "}"));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-type", "application/json")
                .setBody("{\n\"carStatus\": \"FREE\"\n}"));
        ResponseEntity<Object> actual = userService.changeCarStatus(1L, 1L, requestDto);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertTrue(Objects.requireNonNull(actual.getBody()).toString().contains("FREE"));
    }

    @Test
    void changeCarStatusWithRentedCar() throws IOException {
        mockWebServer.start(8082);
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
                () -> userService.changeCarStatus(
                        1L, 1L, requestDto));
        mockWebServer.shutdown();
    }

    @Test
    void changeCarStatusWithBadRequest() throws IOException {
        mockWebServer.start(8082);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\n"
                        + "    \"id\": 1,\n"
                        + "    \"carStatus\": \"LOCKED\",\n"
                        + "    \"carOwnerId\": 1\n"
                        + "}"));
        ChangeCarStatusRequestDto requestDto = new ChangeCarStatusRequestDto();
        requestDto.setStatus("FREE");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400));
        ResponseEntity<Object> actual = userService.changeCarStatus(1L, 1L, requestDto);
        mockWebServer.shutdown();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }
}
