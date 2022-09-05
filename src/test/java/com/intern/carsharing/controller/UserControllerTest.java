package com.intern.carsharing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.ApiExceptionObject;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.dto.request.ChangeStatusRequestDto;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.UserResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.BalanceRepository;
import com.intern.carsharing.repository.StatusRepository;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private StatusRepository statusRepository;
    @MockBean
    private BalanceRepository balanceRepository;
    private MockMvc mockMvc;
    private User userFromDb;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userFromDb = new User();
        userFromDb.setId(1L);
        userFromDb.setEmail("bob@gmail.com");
        userFromDb.setPassword("$2a$10$/xbcBmXcySEczXGThC2Rtu/mR9R9PCkFP5PaCShbGkcwu/frh0mUW");
        userFromDb.setFirstName("Bob");
        userFromDb.setLastName("Alister");
        userFromDb.setAge(21);
        userFromDb.setDriverLicence("DFG23K34H");
        userFromDb.setRoles(Set.of(new Role(1L, RoleName.USER)));
        userFromDb.setStatus(new Status(1L, StatusType.ACTIVE));
    }

    @Test
    void getUserInfoWithExistUserId() throws Exception {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), Set.of(new Role(1L, RoleName.USER)));
        MvcResult mvcResult = mockMvc.perform(get("/users/{id}", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        UserResponseDto actualDto = objectMapper
                .readValue(actualResponseBody, UserResponseDto.class);
        Assertions.assertEquals(actualDto.getId(), userFromDb.getId());
        Assertions.assertEquals(actualDto.getEmail(), userFromDb.getEmail());
    }

    @Test
    void getUserInfoByUserWithAnotherId() throws Exception {
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), Set.of(new Role(1L, RoleName.USER)));
        mockMvc.perform(get("/users/{id}", 2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserInfoWithNotExistUserId() throws Exception {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), Set.of(new Role(1L, RoleName.USER)));
        mockMvc.perform(get("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserInfoWithExpiredToken() throws Exception {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String expiredJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2IxQGdtYWlsLmNvbSIsInJvbGVz"
                + "IjpbIlVTRVIiXSwiaWF0IjoxNjYwODQ3NTMzLCJleHAiOjE2NjA4NDc4OTN9"
                + ".UH-NgeTcF9dFfdVv_GrSmUtjzfN__mgPIS4a3SUsPZk";
        mockMvc.perform(get("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserInfoWithInvalidToken() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid_token"))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        ApiExceptionObject apiExceptionObject = objectMapper
                .readValue(actualResponseBody, ApiExceptionObject.class);
        Assertions.assertEquals(apiExceptionObject.getMessage(), "Jwt token not valid or expired");
        Assertions.assertEquals(apiExceptionObject.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getUserInfoWithEmptyToken() throws Exception {
        mockMvc.perform(get("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isForbidden());
    }

    @Test
    void userUpdateWithValidDataAndToken() throws Exception {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));

        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail("newbob@gmail.com");
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());

        userFromDb.setEmail(userUpdateDto.getEmail());
        Mockito.when(userRepository.save(userFromDb))
                .thenReturn(userFromDb);
        String jwt = jwtTokenProvider
                .createToken("bob@gmail.com", Set.of(new Role(1L, RoleName.USER)));
        MvcResult mvcResult = mockMvc.perform(put("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        UserResponseDto actualResponseDto = objectMapper
                .readValue(actualResponseBody, UserResponseDto.class);
        Assertions.assertEquals(actualResponseDto.getEmail(), userUpdateDto.getEmail());
    }

    @Test
    void userUpdateWithInvalidId() throws Exception {
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail("newbob@gmail.com");
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken("bob@gmail.com", Set.of(new Role(1L, RoleName.USER)));

        mockMvc.perform(put("/users/update/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userUpdateWithExistingEmail() throws Exception {
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail("newbob@gmail.com");
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());

        User anotherUserWithSameEmail = new User();
        anotherUserWithSameEmail.setId(2L);
        anotherUserWithSameEmail.setEmail("newbob@gmail.com");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository
                .findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository
                .findUserByEmail(userUpdateDto.getEmail()))
                .thenReturn(Optional.of(anotherUserWithSameEmail));
        String jwt = jwtTokenProvider
                .createToken("bob@gmail.com", Set.of(new Role(1L, RoleName.USER)));

        mockMvc.perform(put("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void userUpdateWithExistingEmailAndSameId() throws Exception {
        UserUpdateRequestDto userUpdateDto = new UserUpdateRequestDto();
        userUpdateDto.setEmail(userFromDb.getEmail());
        userUpdateDto.setFirstName(userFromDb.getFirstName());
        userUpdateDto.setLastName(userFromDb.getLastName());
        userUpdateDto.setAge(userFromDb.getAge());
        userUpdateDto.setDriverLicence(userFromDb.getDriverLicence());

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository
                        .findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository
                        .findUserByEmail(userUpdateDto.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken("bob@gmail.com", Set.of(new Role(1L, RoleName.USER)));

        mockMvc.perform(put("/users/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatusWithValidData() throws Exception {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("bob1@gmail.com");
        admin.setPassword("$2a$10$/xbcBmXcySEczXGThC2Rtu/mR9R9PCkFP5PaCShbGkcwu/frh0mUW");
        admin.setRoles(Set.of(new Role(2L, RoleName.ADMIN)));
        admin.setStatus(new Status(1L, StatusType.ACTIVE));
        Mockito.when(userRepository.findUserByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));
        Status blocked = new Status(2L, StatusType.BLOCKED);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(userFromDb));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(userFromDb);
        Mockito.when(statusRepository.findByStatusType(StatusType.BLOCKED)).thenReturn(blocked);

        ChangeStatusRequestDto requestDto = new ChangeStatusRequestDto();
        requestDto.setStatus("blocked");
        String jwt = jwtTokenProvider
                .createToken(admin.getEmail(), admin.getRoles());
        mockMvc.perform(patch("/users/{id}", userFromDb.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void toBalanceWithValidData() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(0));
        Mockito.when(balanceRepository.findByUserId(userFromDb.getId()))
                .thenReturn(Optional.of(balance));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.ofNullable(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(patch("/users/{id}/to-balance", userFromDb.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
