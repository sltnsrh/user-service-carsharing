package com.intern.carsharing.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public abstract class IntegrationTest {
    protected static final String USER_EMAIL = "user@gmail.com";
    protected static final int REDIS_HOST = 6379;
    private static final MySQLContainer<?> container;
    private static final GenericContainer<?> redisServer;

    @Autowired
    protected ObjectMapper objectMapper;
    protected MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    static {
        container = new MySQLContainer<>("mysql:latest");
        container.start();
        redisServer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(REDIS_HOST);
        redisServer.start();
        System.setProperty("spring.redis.host", redisServer.getHost());
        System.setProperty("spring.redis.port", redisServer.getMappedPort(REDIS_HOST).toString());
    }

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
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }
}
