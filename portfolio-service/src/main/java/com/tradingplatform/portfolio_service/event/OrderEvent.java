package com.tradingplatform.portfolio_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private String symbol;
    private String orderType;  // BUY or SELL
    private Integer quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private LocalDateTime executedAt;
    private String eventType;  // ORDER_EXECUTED
}
