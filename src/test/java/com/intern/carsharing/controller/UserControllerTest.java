package com.intern.carsharing.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.ApiExceptionObject;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RequestUserUpdateDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
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
        userFromDb.setStatus(new Status(1L, StatusType.ENABLE));
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
        ResponseUserDto actualDto = objectMapper
                .readValue(actualResponseBody, ResponseUserDto.class);
        Assertions.assertEquals(actualDto.getId(), userFromDb.getId());
        Assertions.assertEquals(actualDto.getEmail(), userFromDb.getEmail());
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
    void userUpdateWithValidDataAndToken() throws Exception {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(userFromDb));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));

        RequestUserUpdateDto userUpdateDto = new RequestUserUpdateDto();
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
        MvcResult mvcResult = mockMvc.perform(put("/users/update/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        ResponseUserDto actualResponseDto = objectMapper
                .readValue(actualResponseBody, ResponseUserDto.class);
        Assertions.assertEquals(actualResponseDto.getEmail(), userUpdateDto.getEmail());
    }

    @Test
    void userUpdateWithInvalidId() throws Exception {
        RequestUserUpdateDto userUpdateDto = new RequestUserUpdateDto();
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
        RequestUserUpdateDto userUpdateDto = new RequestUserUpdateDto();
        userUpdateDto.setEmail("newbob@gmail.com");
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

        mockMvc.perform(put("/users/update/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isConflict());
    }
}
