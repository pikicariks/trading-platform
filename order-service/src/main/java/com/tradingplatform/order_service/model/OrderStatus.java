package com.tradingplatform.order_service.model;

public enum OrderStatus {
    PENDING,
    VALIDATING,
    EXECUTING,
    EXECUTED,
    FAILED,
    CANCELLED
}
