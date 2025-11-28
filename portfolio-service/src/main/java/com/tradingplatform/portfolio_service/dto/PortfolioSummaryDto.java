package com.tradingplatform.portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummaryDto {

    private Long userId;
    private BigDecimal totalValue;
    private BigDecimal cashBalance;
    private BigDecimal investedAmount;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
    private Integer totalHoldings;
}
