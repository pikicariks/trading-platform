package com.tradingplatform.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceResponseDto {

    private Long userId;
    private BigDecimal balance;
    private String currency;
    private Boolean hasSufficientFunds;

    public BalanceResponseDto(Long userId, BigDecimal balance, String currency) {
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
        this.hasSufficientFunds = true;
    }
}
