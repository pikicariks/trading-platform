package com.tradingplatform.order_service.dto;

import com.tradingplatform.order_service.model.OrderStatus;
import com.tradingplatform.order_service.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private String symbol;
    private OrderType orderType;
    private OrderStatus status;
    private Integer quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private BigDecimal commission;
    private String notes;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
}
