package com.tradingplatform.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponseDto {

    private Long totalOrders;
    private Long executedOrders;
    private Long pendingOrders;
    private Long failedOrders;
    private BigDecimal totalBuyAmount;
    private BigDecimal totalSellAmount;
    private BigDecimal totalCommission;
}
