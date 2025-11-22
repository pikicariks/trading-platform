package com.tradingplatform.wallet_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance. Required: $%.2f, Available: $%.2f",
                required, available));
    }
}
