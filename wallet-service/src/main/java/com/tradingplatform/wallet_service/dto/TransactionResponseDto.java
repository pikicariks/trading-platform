package com.tradingplatform.wallet_service.dto;

import com.tradingplatform.wallet_service.model.TransactionStatus;
import com.tradingplatform.wallet_service.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private Long walletId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceId;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
