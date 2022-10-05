package com.intern.carsharing.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.model.RefreshToken;
import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.Status;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.RefreshTokenRequestDto;
import com.intern.carsharing.model.dto.request.ValidateTokenRequestDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.model.util.StatusType;
import com.intern.carsharing.repository.RefreshTokenRepository;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthenticationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void refreshTokenWithValidRefreshToken() throws Exception {
        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        refreshToken.setToken("refreshtoken");
        Mockito.when(refreshTokenRepository.findByToken("refreshtoken"))
                .thenReturn(Optional.of(refreshToken));

        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");
        mockMvc.perform(post("/refresh-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshTokenWithExpiredRefreshToken() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(new User());
        refreshToken.setExpiredAt(LocalDateTime.now().minusMinutes(15));
        refreshToken.setToken("refreshtoken");
        Mockito.when(refreshTokenRepository.findByToken("refreshtoken"))
                .thenReturn(Optional.of(refreshToken));

        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
        requestDto.setToken("refreshtoken");
        mockMvc.perform(post("/refresh-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateTokenWithValidTokenAndActiveUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setStatus(new Status(1L, StatusType.ACTIVE));
        user.setRoles(Set.of(new Role(1L, RoleName.USER)));
        String jwt = jwtTokenProvider
                .createToken(user.getEmail(), user.getRoles());
        Mockito.when(userRepository.findUserByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        ValidateTokenRequestDto requestDto = new ValidateTokenRequestDto();
        requestDto.setToken("Bearer " + jwt);
        mockMvc.perform(get("/validate-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
