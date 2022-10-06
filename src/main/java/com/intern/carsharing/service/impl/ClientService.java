package com.intern.carsharing.service.impl;

import com.intern.carsharing.exception.ApiExceptionObject;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public abstract class ClientService {

    protected MultiValueMap<String, String> getPresentQueryParams(
            String startDate, String endDate, String carType
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (carType != null) {
            queryParams.add("carType", carType);
        }
        if (startDate != null) {
            queryParams.add("dateStart", startDate);
        }
        if (endDate != null) {
            queryParams.add("dateEnd", endDate);
        }
        return queryParams;
    }

    protected ResponseEntity<Object> createResponseEntityException(
            WebClientResponseException e
    ) {
        ApiExceptionObject apiExceptionObject = new ApiExceptionObject(
                e.getMessage(), e.getStatusCode(), LocalDateTime.now().toString()
        );
        return new ResponseEntity<>(apiExceptionObject, e.getStatusCode());
    }
}
