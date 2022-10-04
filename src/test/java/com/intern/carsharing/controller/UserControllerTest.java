package com.intern.carsharing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.model.Balance;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.BalanceRequestDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.BalanceRepository;
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
    void toBalanceWithValidData() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(0));
        Mockito.when(balanceRepository.findByUserId(userFromDb.getId()))
                .thenReturn(Optional.of(balance));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        mockMvc.perform(patch("/users/{id}/to-balance", userFromDb.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void fromBalanceEnoughMoneyCase() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(100));
        balance.setCurrency("UAH");
        userFromDb.setRoles(Set.of(new Role(2L, RoleName.ADMIN)));
        Mockito.when(balanceRepository.findByUserId(userFromDb.getId()))
                .thenReturn(Optional.of(balance));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        Mockito.when(balanceRepository.save(any(Balance.class))).thenReturn(null);
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        MvcResult mvcResult = mockMvc.perform(patch("/users/{id}/from-balance", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String actual = mvcResult
                .getResponse().getContentAsString();
        Assertions.assertEquals("100 UAH were debited from the balance of the user with id 1",
                actual);
    }

    @Test
    void fromBalanceNotEnoughMoneyCase() throws Exception {
        BalanceRequestDto requestDto = new BalanceRequestDto();
        requestDto.setValue(BigDecimal.valueOf(100));
        Balance balance = new Balance();
        balance.setValue(BigDecimal.valueOf(99));
        balance.setCurrency("UAH");
        userFromDb.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        Mockito.when(balanceRepository.findByUserId(userFromDb.getId()))
                .thenReturn(Optional.of(balance));
        Mockito.when(userRepository.findUserByEmail(userFromDb.getEmail()))
                .thenReturn(Optional.of(userFromDb));
        String jwt = jwtTokenProvider
                .createToken(userFromDb.getEmail(), userFromDb.getRoles());
        MvcResult mvcResult = mockMvc.perform(patch("/users/{id}/from-balance", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        String actual = mvcResult
                .getResponse().getContentAsString();
        Assertions.assertEquals("Not enough money on balance for a transaction",
                actual);
    }
}
