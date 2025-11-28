package com.tradingplatform.portfolio_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(Long userId) {
        super("Portfolio not found for user: " + userId);
    }
}
