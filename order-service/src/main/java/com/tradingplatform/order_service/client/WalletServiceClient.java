package com.tradingplatform.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service")
public interface WalletServiceClient {

    @GetMapping("/api/wallet/user/{userId}/balance")
    BalanceResponse getBalance(@PathVariable("userId") Long userId);

    @PostMapping("/api/wallet/user/{userId}/deduct")
    TransactionResponse deductForPurchase(
            @PathVariable("userId") Long userId,
            @RequestBody TransactionRequest request
    );

    @PostMapping("/api/wallet/user/{userId}/credit")
    TransactionResponse creditFromSale(
            @PathVariable("userId") Long userId,
            @RequestBody TransactionRequest request
    );

    class BalanceResponse {
        public Long userId;
        public BigDecimal balance;
        public String currency;
        public Boolean hasSufficientFunds;
    }

    class TransactionRequest {
        public BigDecimal amount;
        public String description;
        public String referenceId;

        public TransactionRequest(BigDecimal amount, String description, String referenceId) {
            this.amount = amount;
            this.description = description;
            this.referenceId = referenceId;
        }
    }

    class TransactionResponse {
        public Long id;
        public String transactionType;
        public BigDecimal amount;
        public BigDecimal balanceAfter;
    }
}
