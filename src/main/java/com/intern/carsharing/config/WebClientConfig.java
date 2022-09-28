package com.intern.carsharing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${backoffice.service.host}")
    private String backOfficeServiceHost;
    @Value("${car.service.host}")
    private String carServiceHost;

    @Bean
    public WebClient backofficeServiceClient() {
        return WebClient.create(backOfficeServiceHost);
    }

    @Bean
    public WebClient carServiceClient() {
        return WebClient.create(carServiceHost);
    }
}
