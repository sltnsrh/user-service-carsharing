package com.intern.carsharing.service.impl;

import com.intern.carsharing.service.PermissionService;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class BackofficeClientServiceImplTest {
    @InjectMocks
    private BackofficeClientServiceImpl backofficeClientService;
    @Mock
    private PermissionService permissionService;
    @Spy
    private final WebClient backofficeClient = WebClient.create("http://localhost:8082");
    private final MockWebServer mockWebServer = new MockWebServer();

    @BeforeEach
    void start() throws IOException {
        mockWebServer.start(8082);
    }

    @AfterEach
    void close() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getTripStatisticsWithValidUserIdAndParameters() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\"trip\":\"1\"}"));

        Object actual = backofficeClientService.getTripStatistics(
                1L,
                "2022-01-01",
                "2022-02-01",
                "PREMIUM"
        );
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("{trip=1}", actual.toString());
    }

    @Test
    void getTripStatisticsWithValidUserId() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-type", "application/json")
                .setBody("{\"trip\":\"1\"}"));

        Object actual = backofficeClientService.getTripStatistics(
                1L, null, null, null);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("{trip=1}", actual.toString());
    }
}
