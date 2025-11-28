package com.tradingplatform.portfolio_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoldingResponseDto {

    private Long id;
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal totalInvested;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;
}
