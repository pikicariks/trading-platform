package com.tradingplatform.market_data_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
