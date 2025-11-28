package com.tradingplatform.portfolio_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientSharesException extends RuntimeException {
    public InsufficientSharesException(String symbol, int available, int required) {
        super(String.format("Insufficient shares of %s. Available: %d, Required: %d",
                symbol, available, required));
    }
}
