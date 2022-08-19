package com.intern.carsharing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.ApiExceptionObject;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.LoginRequestDto;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.LoginResponseDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthenticationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void registerWithValidData() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");

        ResponseUserDto responseUserDto = new ResponseUserDto();
        responseUserDto.setId(1L);
        responseUserDto.setEmail("bob@gmail.com");
        responseUserDto.setPassword("password");
        responseUserDto.setFirstName("Bob");
        responseUserDto.setLastName("Alister");
        responseUserDto.setAge(21);
        responseUserDto.setDriverLicence("DFG23K34H");
        responseUserDto.setRoles(Set.of("ADMIN"));
        responseUserDto.setStatus("ENABLE");

        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Alister");
        user.setAge(21);
        user.setDriverLicence("DFG23K34H");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        user.setStatus(new Status(1L, StatusType.ENABLE));

        Mockito.when(userRepository.findUserByEmail(requestUserDto.getEmail()))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        MvcResult mvcResult = mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isCreated())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertEquals(actualResponseBody,
                objectMapper.writeValueAsString(responseUserDto));
    }

    @Test
    void registerWithNotMatchPassword() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("passwor");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");

        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody.contains("Passwords do not match"));
    }

    @Test
    void registerExistUser() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");

        Mockito.when(userRepository.findUserByEmail(requestUserDto.getEmail()))
                .thenReturn(Optional.of(new User()));
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isConflict())
                .andReturn();
        String actualResponseBody = mvcResult
                .getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody
                .contains("User with email bob@gmail.com already exists"));
    }

    @Test
    void registerWithAgeLessThan21() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(20);
        requestUserDto.setDriverLicence("DFG23K34H");

        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody
                .contains("Your age must be at least 21 years old"));
    }

    @Test
    void loginWithValidData() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");

        User userFromDb = new User();
        userFromDb.setId(1L);
        userFromDb.setEmail("bob@gmail.com");
        userFromDb.setPassword("$2a$10$/xbcBmXcySEczXGThC2Rtu/mR9R9PCkFP5PaCShbGkcwu/frh0mUW");
        userFromDb.setFirstName("bob");
        userFromDb.setLastName("Alister");
        userFromDb.setAge(21);
        userFromDb.setDriverLicence("234WER234");
        userFromDb.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        userFromDb.setStatus(new Status(1L, StatusType.ENABLE));

        Mockito.when(userRepository.findUserByEmail(loginRequestDto.getEmail()))
                .thenReturn(Optional.of(userFromDb));

        MvcResult mvcResult = mockMvc.perform(post("/login")
                .with(user("bob@gmail.com").password("password").roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        LoginResponseDto loginResponseDto = objectMapper
                .readValue(actualResponseBody, LoginResponseDto.class);

        Assertions.assertEquals(loginRequestDto.getEmail(), loginResponseDto.getEmail());
        Assertions.assertFalse(loginResponseDto.getToken().isBlank());
    }

    @Test
    void loginWithInvalidPassword() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("passwor");

        User userFromDb = new User();
        userFromDb.setId(1L);
        userFromDb.setEmail("bob@gmail.com");
        userFromDb.setPassword("$2a$10$/xbcBmXcySEczXGThC2Rtu/mR9R9PCkFP5PaCShbGkcwu/frh0mUW");
        userFromDb.setFirstName("bob");
        userFromDb.setLastName("Alister");
        userFromDb.setAge(21);
        userFromDb.setDriverLicence("234WER234");
        userFromDb.setRoles(Set.of(new Role(1L, RoleName.valueOf("ADMIN"))));
        userFromDb.setStatus(new Status(1L, StatusType.ENABLE));

        Mockito.when(userRepository.findUserByEmail(loginRequestDto.getEmail()))
                .thenReturn(Optional.of(userFromDb));

        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .with(user("bob@gmail.com").password("passwor").roles("ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ApiExceptionObject apiExceptionObject = objectMapper
                .readValue(actualResponseBody, ApiExceptionObject.class);
        Assertions.assertEquals(apiExceptionObject.getMessage(), "Wrong password, try again");
        Assertions.assertEquals(apiExceptionObject.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithEmptyEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("");
        loginRequestDto.setPassword("password");

        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ApiExceptionObject apiExceptionObject = objectMapper
                .readValue(actualResponseBody, ApiExceptionObject.class);

        Assertions.assertEquals(apiExceptionObject.getMessage(),
                "Email field can't be empty or blank");
        Assertions.assertEquals(apiExceptionObject.getHttpStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginWithNotExistEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("bob@gmail.com");
        loginRequestDto.setPassword("password");
        Mockito.when(userRepository.findUserByEmail(loginRequestDto.getEmail()))
                .thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ApiExceptionObject apiExceptionObject = objectMapper
                .readValue(actualResponseBody, ApiExceptionObject.class);

        Assertions.assertEquals(apiExceptionObject.getMessage(),
                "User with email: bob@gmail.com doesn't exist");
        Assertions.assertEquals(apiExceptionObject.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }
}
