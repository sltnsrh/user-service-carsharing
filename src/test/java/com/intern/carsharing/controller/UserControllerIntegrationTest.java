package com.intern.carsharing.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.dto.request.UserUpdateRequestDto;
import com.intern.carsharing.model.dto.response.UserResponseDto;
import com.intern.carsharing.model.util.RoleName;
import com.intern.carsharing.repository.RoleRepository;
import com.intern.carsharing.repository.UserRepository;
import com.intern.carsharing.security.jwt.JwtTokenProvider;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class UserControllerIntegrationTest {
    private static final String USER_EMAIL = "user@gmail.com";
    private static final String ADMIN_EMAIL = "admin@carsharing.com";
    private static final String NEW_USER_EMAIL = "newuser@gmail.com";
    @Container
    private static final MySQLContainer container = new MySQLContainer<>("mysql:latest");
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private WebApplicationContext applicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.flyway.url", container::getJdbcUrl);
        registry.add("spring.flyway.username", container::getUsername);
        registry.add("spring.flyway.password", container::getPassword);
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

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
        String jwt = jwtTokenProvider
                .createToken(ADMIN_EMAIL,
                        Set.of(roleRepository.findByRoleName(RoleName.ADMIN)));
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
        String jwt = jwtTokenProvider
                .createToken(ADMIN_EMAIL,
                        Set.of(roleRepository.findByRoleName(RoleName.ADMIN)));
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
}
