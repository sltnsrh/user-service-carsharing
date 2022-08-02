package com.intern.carsharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.exception.UserAlreadyExistException;
import com.intern.carsharing.model.dto.request.RegistrationRequestUserDto;
import com.intern.carsharing.model.dto.response.ResponseUserDto;
import com.intern.carsharing.service.AuthService;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {
    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ValidData_StatusCreated() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));

        ResponseUserDto responseUserDto = new ResponseUserDto();
        responseUserDto.setId(1L);
        responseUserDto.setEmail("bob@gmail.com");
        responseUserDto.setPassword("password");
        responseUserDto.setFirstName("Bob");
        responseUserDto.setLastName("Alister");
        responseUserDto.setAge(21);
        responseUserDto.setDriverLicence("DFG23K34H");
        responseUserDto.setRoles(Set.of("ADMIN"));
        responseUserDto.setStatus("ENABLE");;

        Mockito.when(authService.register(eq(requestUserDto))).thenReturn(responseUserDto);
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isCreated())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertEquals(actualResponseBody, objectMapper.writeValueAsString(responseUserDto));
    }

    @Test
    void register_passwordNotMatch_throw400() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("passwor");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));

        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody.contains("Passwords do not match"));
    }

    @Test
    void register_passwordExists_throw409() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(21);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));

        Mockito.doThrow(new UserAlreadyExistException("User with email bob@gmail.com is already exist"))
                .when(authService).register(eq(requestUserDto));
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isConflict())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody.contains("User with email bob@gmail.com is already exist"));
    }

    @Test
    void register_ageLessThan21_throw400() throws Exception {
        RegistrationRequestUserDto requestUserDto = new RegistrationRequestUserDto();
        requestUserDto.setEmail("bob@gmail.com");
        requestUserDto.setPassword("password");
        requestUserDto.setRepeatPassword("password");
        requestUserDto.setFirstName("Bob");
        requestUserDto.setLastName("Alister");
        requestUserDto.setAge(20);
        requestUserDto.setDriverLicence("DFG23K34H");
        requestUserDto.setRoles(Set.of("ADMIN"));

        MvcResult mvcResult = mockMvc.perform(post("/registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestUserDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(actualResponseBody.contains("Your age must be at least 21 years old"));
    }
}
